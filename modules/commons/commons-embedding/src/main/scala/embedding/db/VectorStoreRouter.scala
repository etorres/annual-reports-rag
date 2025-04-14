package es.eriktorr
package embedding.db

import common.api.DocumentMetadata
import embedding.db.ElasticError.ListIndexesFailed

import cats.effect.IO
import cats.implicits.*
import co.elastic.clients.elasticsearch.indices.IndicesStatsRequest
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import org.typelevel.log4cats.Logger

import scala.jdk.CollectionConverters.given

trait VectorStoreRouter extends ElasticVectorStore:
  def bestMatchFor(question: String, maxResults: Int): IO[List[VectorResult]]

object VectorStoreRouter:
  def impl(
      elasticClient: ElasticClient,
      key: DocumentMetadata,
  )(using logger: Logger[IO]): VectorStoreRouter =
    new VectorStoreRouter:
      override def bestMatchFor(
          question: String,
          maxResults: Int,
      ): IO[List[VectorResult]] =
        for
          indices <- listAllIndices()
          vectorResults <- indices.flatTraverse: index =>
            bestMatchFor(index, question, maxResults)
        yield vectorResults

      private def listAllIndices() =
        for
          response <- IO.blocking:
            elasticClient.elasticsearchClient
              .indices()
              .stats(IndicesStatsRequest.Builder().build())
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
              .filter(_.embedded().metadata().containsKey(key.name))
              .sortBy(_.score())
              .takeRight(maxResults)
          _ <- logger.debug(s"$index produces ${embeddingMatches.length} matches")
          result = embeddingMatches.map: embeddingMatch =>
            VectorResult(
              id = embeddingMatch.embeddingId(),
              index = embeddingMatch.embedded().metadata().getString(key.name),
              score = embeddingMatch.score(),
              text = embeddingMatch.embedded().text(),
            )
        yield result
