package es.eriktorr

final class VectorStoreBuilder(elasticConfig: ElasticConfig):
  def impl(index: String): VectorStore =
    VectorStore.impl(elasticConfig, index)
