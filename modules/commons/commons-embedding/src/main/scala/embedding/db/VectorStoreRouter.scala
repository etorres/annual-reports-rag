package es.eriktorr
package embedding.db

import common.api.DocumentMetadata
import embedding.db.ElasticError.{EmbeddingSearchFailed, ListIndexesFailed, QueryFailed}
import embedding.db.ElasticExtensions.unique
import embedding.db.JsonDataListExtensions.{integerFrom, textFrom}

import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import cats.implicits.{catsSyntaxParallelTraverse1, catsSyntaxTuple4Semigroupal, toTraverseOps}
import co.elastic.clients.elasticsearch.sql.QueryRequest
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import org.typelevel.log4cats.Logger

import scala.jdk.CollectionConverters.given

trait VectorStoreRouter extends ElasticVectorStore:
  def findBy(index: String, pages: NonEmptyList[Int]): IO[List[DocumentResult]]
  def topNRelevantChunks(n: Int, question: Question): OptionT[IO, ChunkSet]

object VectorStoreRouter:
  def impl(elasticClient: ElasticClient)(using logger: Logger[IO]): VectorStoreRouter =
    new VectorStoreRouter:
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

      override def topNRelevantChunks(
          n: Int,
          question: Question,
      ): OptionT[IO, ChunkSet] =
        for
          index <- indexFrom(question)
          chunkSet <- bestMatchFor(index, question.question, n)
        yield chunkSet

      private def indexFrom(question: Question): OptionT[IO, String] =
        OptionT:
          for
            allIndices <- listAllIndices()
            maybeIndex <- allIndices
              .parTraverse: index =>
                matches(index, question.companyName)
                  .ifM(ifTrue = IO.some(index), ifFalse = IO.none)
              .map(_.collectFirst { case Some(value) => value })
          yield maybeIndex

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
        OptionT:
          for
            embeddingStore <- embeddingStoreFrom(elasticClient, index)
            queryEmbedding = embeddingModel.embed(question).content()
            request = EmbeddingSearchRequest
              .builder()
              .queryEmbedding(queryEmbedding)
              .maxResults(maxResults)
              .build()
            response <- IO.blocking(embeddingStore.search(request))
            embeddingMatches =
              response
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
            fileToEmbeddingMatches = embeddingMatches.groupMap { embeddingMatch =>
              val metadata = embeddingMatch.embedded().metadata()
              val filename = metadata.getString(DocumentMetadata.Filename.name)
              val fileChecksum = metadata.getString(DocumentMetadata.Sha1FileChecksum.name)
              val index = elasticClient.indexNameFrom(fileChecksum)
              filename -> index
            } { embeddingMatch =>
              val metadata = embeddingMatch.embedded().metadata()
              ChunkSet.Chunk(
                page = metadata.getInteger(DocumentMetadata.Page.name),
                score = embeddingMatch.score(),
                text = embeddingMatch.embedded().text(),
              )
            }
            chunkSet <- fileToEmbeddingMatches.toList match
              case Nil => IO.none
              case ((filename, index), chunks) :: Nil =>
                IO.pure(
                  NonEmptyList
                    .fromList(chunks.sortBy(_.score).reverse.take(maxResults))
                    .map(ChunkSet(filename, index, _)),
                )
              case ::(head, next) => IO.raiseError(EmbeddingSearchFailed(index))
          yield chunkSet

      private def matches(index: String, companyName: String) =
        val sql =
          s"""SELECT ${columnFrom(DocumentMetadata.CompanyName)}
             |FROM $index
             |LIMIT 1""".stripMargin
        for
          (columns, row) <- runQuery(sql).unique
          obtained <- IO.fromEither:
            row.textFrom(columns, columnFrom(DocumentMetadata.CompanyName))
        yield obtained == companyName

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
