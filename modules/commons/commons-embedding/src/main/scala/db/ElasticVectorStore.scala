package es.eriktorr
package db

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore
import org.elasticsearch.client.RestClient

trait ElasticVectorStore:
  val embeddingModel: EmbeddingModel = ElasticVectorStore.embeddingModel
  def embeddingStoreFrom(restClient: RestClient, indexName: String): EmbeddingStore[TextSegment] =
    ElasticsearchEmbeddingStore
      .builder()
      .restClient(restClient)
      .indexName(indexName)
      .build()

object ElasticVectorStore:
  val embeddingModel: EmbeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel()
