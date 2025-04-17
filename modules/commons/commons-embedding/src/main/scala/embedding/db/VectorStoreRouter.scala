package es.eriktorr
package embedding.db

import common.api.DocumentMetadata
import embedding.db.ElasticError.{ListIndexesFailed, QueryFailed}
import embedding.db.JsonDataListExtensions.{integerFrom, textFrom}

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.{catsSyntaxParallelFlatTraverse1, catsSyntaxTuple4Semigroupal, toTraverseOps}
import co.elastic.clients.elasticsearch.sql.QueryRequest
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import org.typelevel.log4cats.Logger

import scala.jdk.CollectionConverters.given

trait VectorStoreRouter extends ElasticVectorStore:
  def bestMatchFor(question: String, maxResults: Int): IO[List[VectorResult]]
  def findBy(index: String, pages: NonEmptyList[Int]): IO[List[DocumentResult]]

object VectorStoreRouter:
  def impl(elasticClient: ElasticClient)(using logger: Logger[IO]): VectorStoreRouter =
    new VectorStoreRouter:
      override def bestMatchFor(
          question: String,
          maxResults: Int,
      ): IO[List[VectorResult]] =
        for
          indices <- listAllIndices()
          vectorResults <- indices.parFlatTraverse: index =>
            bestMatchFor(index, question, maxResults)
        yield vectorResults

      private def listAllIndices() =
        for
          response <- IO.blocking:
            elasticClient.elasticsearchClient.indices().stats()
          shards = response.shards()
          _ <-
            if shards.failed().doubleValue() > 0d then
              IO.raiseError(ListIndexesFailed(shards.failed().doubleValue()))
            else IO.unit
          indices = response
            .indices()
            .keySet()
            .asScala
            .toList
            .filter(_.startsWith(elasticClient.elasticConfig.namespace))
        yield indices

      private def bestMatchFor(index: String, question: String, maxResults: Int) =
        for
          embeddingStore <- embeddingStoreFrom(elasticClient, index)
          queryEmbedding = embeddingModel.embed(question).content()
          request = EmbeddingSearchRequest
            .builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResults)
            .build()
          relevant <- IO.blocking(embeddingStore.search(request))
          embeddingMatches =
            relevant
              .matches()
              .asScala
              .toList
              .filter: embeddingMatch =>
                val metadata = embeddingMatch.embedded().metadata()
                List(
                  DocumentMetadata.Page,
                  DocumentMetadata.Sha1FileChecksum,
                )
                  .forall: documentMetadata =>
                    metadata.containsKey(documentMetadata.name)
              .sortBy(_.score())
              .takeRight(maxResults)
          _ <- logger.debug(s"$index produces ${embeddingMatches.length} matches")
          result = embeddingMatches.map: embeddingMatch =>
            val metadata = embeddingMatch.embedded().metadata()
            VectorResult(
              id = embeddingMatch.embeddingId(),
              index = metadata.getString(DocumentMetadata.Sha1FileChecksum.name),
              page = metadata.getInteger(DocumentMetadata.Page.name),
              score = embeddingMatch.score(),
              text = embeddingMatch.embedded().text(),
            )
        yield result

      override def findBy(index: String, pages: NonEmptyList[Int]): IO[List[DocumentResult]] =
        val pagesFilter = pages.toList.mkString("(", ",", ")")
        val sql =
          s"""SELECT
             |  ${columnFrom(DocumentMetadata.Chunk)},
             |  ${columnFrom(DocumentMetadata.Filename)},
             |  ${columnFrom(DocumentMetadata.Page)},
             |  text
             |FROM $index
             |WHERE ${columnFrom(DocumentMetadata.Page)} IN $pagesFilter""".stripMargin
        for
          (columns, rows) <- runQuery(sql)
          documentResults <- rows.traverse: values =>
            IO.fromEither:
              (
                values.integerFrom(columns, columnFrom(DocumentMetadata.Chunk)),
                values.textFrom(columns, columnFrom(DocumentMetadata.Filename)),
                values.integerFrom(columns, columnFrom(DocumentMetadata.Page)),
                values.textFrom(columns, "text"),
              ).tupled.map:
                case (chunk, filename, page, text) =>
                  DocumentResult(chunk, filename, page, text)
        yield documentResults

      private def columnFrom(documentMetadata: DocumentMetadata) =
        s"metadata.${documentMetadata.name}"

      private def runQuery(sql: String) =
        for
          queryResponse <- IO.blocking:
            elasticClient.elasticsearchClient.sql().query(QueryRequest.Builder().query(sql).build())
          (columns, rows) <-
            if queryResponse.isRunning || queryResponse.isPartial then
              IO.raiseError(QueryFailed(sql))
            else
              IO.pure:
                (
                  queryResponse.columns().asScala.toList,
                  queryResponse.rows().asScala.toList.map(_.asScala.toList),
                )
        yield (columns, rows)
