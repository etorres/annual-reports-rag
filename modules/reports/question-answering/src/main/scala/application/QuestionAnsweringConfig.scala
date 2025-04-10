package es.eriktorr
package application

import api.OllamaModel

final case class QuestionAnsweringConfig(elasticConfig: ElasticConfig, ollamaConfig: OllamaConfig)

object QuestionAnsweringConfig:
  def singleProcessor(ollamaModel: OllamaModel): QuestionAnsweringConfig =
    QuestionAnsweringConfig(
      ElasticConfig.localContainerFor("embeddings"),
      OllamaConfig.localContainerFor(ollamaModel, 1),
    )
