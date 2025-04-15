package es.eriktorr
package report.application

import embedding.application.ElasticConfig
import ollama.api.OllamaModel
import ollama.application.OllamaConfig

final case class ReportLoaderConfig(
    elasticConfig: ElasticConfig,
    maximumParallelism: Int,
    ollamaConfig: OllamaConfig,
)

object ReportLoaderConfig:
  def localContainerFor(maximumParallelism: Int, ollamaModel: OllamaModel): ReportLoaderConfig =
    ReportLoaderConfig(
      ElasticConfig.localContainerFor("embeddings"),
      maximumParallelism,
      OllamaConfig.localContainerFor(ollamaModel),
    )
