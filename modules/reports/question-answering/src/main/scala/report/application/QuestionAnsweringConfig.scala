package es.eriktorr
package report.application

import embedding.application.ElasticConfig
import ollama.api.OllamaModel
import ollama.application.OllamaConfig

final case class QuestionAnsweringConfig(
    elasticConfig: ElasticConfig,
    ollamaConfig: OllamaConfig,
    topN: Int,
)

object QuestionAnsweringConfig:
  def localContainerFor(ollamaModel: OllamaModel): QuestionAnsweringConfig =
    QuestionAnsweringConfig(
      elasticConfig = ElasticConfig.localContainerFor("embeddings"),
      ollamaConfig = OllamaConfig.localContainerFor(ollamaModel),
      topN = 30,
    )
