package es.eriktorr

import AnyRefExtensions.ignoreResult

import dev.langchain4j.data.segment.TextSegment
import org.elasticsearch.client.RestClient

import scala.jdk.CollectionConverters.given
import scala.util.Using

trait VectorStore extends ElasticVectorStore:
  def add(textSegment: TextSegment): Unit
  def add(textSegments: List[TextSegment]): Unit

object VectorStore:
  def impl(elasticConfig: ElasticConfig, index: String): VectorStore =
    new VectorStore:
      private val indexName = elasticConfig.indexNameFrom(index)
      override def add(textSegment: TextSegment): Unit =
        Using(RestClient.builder(elasticConfig.httpHost).build()): restClient =>
          val embeddingStore = embeddingStoreFrom(restClient, indexName)
          val embedding = embeddingModel.embed(textSegment).content()
          embeddingStore.add(embedding, textSegment).ignoreResult()
        .get
      override def add(textSegments: List[TextSegment]): Unit =
        Using(RestClient.builder(elasticConfig.httpHost).build()): restClient =>
          val embeddingStore = embeddingStoreFrom(restClient, indexName)
          val embedded = textSegments.asJava
          val embeddings = embeddingModel.embedAll(embedded).content()
          embeddingStore.addAll(embeddings, embedded).ignoreResult()
        .get
