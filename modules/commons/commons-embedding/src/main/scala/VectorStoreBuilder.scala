package es.eriktorr

final class VectorStoreBuilder(elasticConfig: ElasticConfig, prefix: String):
  def impl(filename: String): VectorStore =
    VectorStore.impl(elasticConfig, s"${prefix}_$filename")
