package es.eriktorr
package embedding.db

final class VectorStoreBuilder(elasticClient: ElasticClient):
  def impl(index: String): VectorStore =
    VectorStore.impl(elasticClient, index)
