package es.eriktorr
package embedding.db

import cats.effect.IO
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore

trait ElasticVectorStore:
  val embeddingModel: EmbeddingModel = ElasticVectorStore.embeddingModel
  def embeddingStoreFrom(
      elasticClient: ElasticClient,
      indexName: String,
  ): IO[ElasticsearchEmbeddingStore] =
    IO.delay:
      ElasticsearchEmbeddingStore
        .builder()
        .restClient(elasticClient.restClient)
        .indexName(indexName)
        .build()

object ElasticVectorStore:
  val embeddingModel: EmbeddingModel = AllMiniLmL6V2QuantizedEmbeddingModel()
