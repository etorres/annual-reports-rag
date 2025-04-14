package es.eriktorr
package report.application

import embedding.application.ElasticConfig
import ollama.api.OllamaModel
import ollama.application.OllamaConfig

final case class QuestionAnsweringConfig(elasticConfig: ElasticConfig, ollamaConfig: OllamaConfig)

object QuestionAnsweringConfig:
  def localContainerFor(ollamaModel: OllamaModel): QuestionAnsweringConfig =
    QuestionAnsweringConfig(
      ElasticConfig.localContainerFor("embeddings"),
      OllamaConfig.localContainerFor(ollamaModel),
    )
