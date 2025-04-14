package es.eriktorr
package report.application

import embedding.application.ElasticConfig

final case class ReportLoaderConfig(elasticConfig: ElasticConfig)

object ReportLoaderConfig:
  val localContainer: ReportLoaderConfig =
    ReportLoaderConfig(
      ElasticConfig.localContainerFor("embeddings"),
    )
