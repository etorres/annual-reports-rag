package es.eriktorr
package db

import api.DocumentMetadata
import application.ElasticConfig

import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import org.elasticsearch.client.{Request, RestClient}

import scala.jdk.CollectionConverters.given
import scala.util.Using

trait VectorStoreRouter extends ElasticVectorStore:
  def bestMatchFor(question: String, maxResults: Int): List[VectorResult]
  def indexNameFor(question: String): Option[String]
  def refreshIndex(): Unit

object VectorStoreRouter extends LazyLogging:
  def impl(
      elasticConfig: ElasticConfig,
      index: String,
      key: DocumentMetadata,
  ): VectorStoreRouter =
    new VectorStoreRouter:
      private val indexName = elasticConfig.indexNameFrom(index)
      override def bestMatchFor(question: String, maxResults: Int): List[VectorResult] =
        Using(RestClient.builder(elasticConfig.httpHost).build()): restClient =>
          val queryEmbedding = embeddingModel.embed(question).content()
          val request = EmbeddingSearchRequest
            .builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResults)
            .build()
          val embeddingStore = embeddingStoreFrom(restClient, indexName)
          val relevant = embeddingStore.search(request)
          val embeddingMatches =
            relevant
              .matches()
              .asScala
              .toList
              .filter(_.embedded().metadata().containsKey(key.name))
              .sortBy(_.score())
              .takeRight(maxResults)
          logger.info(s"${embeddingMatches.length} matches")
          embeddingMatches.map: embeddingMatch =>
            VectorResult(
              id = embeddingMatch.embeddingId(),
              indexName = embeddingMatch.embedded().metadata().getString(key.name),
              score = embeddingMatch.score(),
              text = embeddingMatch.embedded().text(),
            )
        .get
      override def indexNameFor(question: String): Option[String] =
        bestMatchFor(question, 1).headOption.map(_.indexName)
      override def refreshIndex(): Unit =
        Using(RestClient.builder(elasticConfig.httpHost).build()): restClient =>
          val response = restClient.performRequest(Request("POST", s"/$indexName/_refresh"))
          val statusCode = response.getStatusLine.getStatusCode
          assert(
            statusCode == 200,
            s"Server status code is $statusCode when trying to refresh the index",
          )
        .get
