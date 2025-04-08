package es.eriktorr
package application

final case class ReportLoaderConfig(
    elasticConfig: ElasticConfig,
    ollamaConfig: OllamaConfig,
)

object ReportLoaderConfig:
  def singleProcessor(ollamaModel: OllamaModel): ReportLoaderConfig =
    ReportLoaderConfig(
      ElasticConfig.localContainerFor("embeddings"),
      OllamaConfig.localContainerFor(ollamaModel, 1),
    )
