package es.eriktorr

import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import org.elasticsearch.client.{Request, RestClient}

import scala.util.Using

trait VectorStoreRouter extends ElasticVectorStore:
  def indexNameFor(question: String): Option[String]
  def refreshIndex(): Unit

object VectorStoreRouter extends LazyLogging:
  def impl(
      elasticConfig: ElasticConfig,
      index: String,
      key: String,
  ): VectorStoreRouter =
    new VectorStoreRouter:
      private val indexName = elasticConfig.indexNameFrom(index)
      override def indexNameFor(question: String): Option[String] =
        Using(RestClient.builder(elasticConfig.httpHost).build()): restClient =>
          val queryEmbedding = embeddingModel.embed(question).content()
          val request = EmbeddingSearchRequest
            .builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(1)
            .build()
          val embeddingStore = embeddingStoreFrom(restClient, indexName)
          val relevant = embeddingStore.search(request)
          val embeddingMatch = relevant.matches().get(0)
          logger.info(s"${embeddingMatch.embeddingId()} scores ${embeddingMatch.score()}")
          val metadata = embeddingMatch.embedded().metadata()
          if metadata.containsKey(key) then Some(metadata.getString(key)) else None
        .get
      override def refreshIndex(): Unit =
        Using(RestClient.builder(elasticConfig.httpHost).build()): restClient =>
          val response = restClient.performRequest(Request("POST", s"/$indexName/_refresh"))
          val statusCode = response.getStatusLine.getStatusCode
          assert(
            statusCode == 200,
            s"Server status code is $statusCode when trying to refresh the index",
          )
        .get
