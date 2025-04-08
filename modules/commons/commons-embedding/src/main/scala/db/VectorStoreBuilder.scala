package es.eriktorr
package db

import application.ElasticConfig

final class VectorStoreBuilder(elasticConfig: ElasticConfig):
  def impl(index: String): VectorStore =
    VectorStore.impl(elasticConfig, index)
