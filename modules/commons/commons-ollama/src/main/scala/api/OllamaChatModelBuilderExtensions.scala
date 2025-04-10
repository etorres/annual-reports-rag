package es.eriktorr
package api

import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.ollama.OllamaChatModel.OllamaChatModelBuilder

object OllamaChatModelBuilderExtensions:
  extension (self: OllamaChatModelBuilder)
    def responseFormat(model: OllamaModel): OllamaChatModelBuilder =
      val responseFormat = model match
        case OllamaModel.Llama3_2 | OllamaModel.TinyLlama => ResponseFormat.JSON
        case _ => ResponseFormat.TEXT
      self.responseFormat(responseFormat)
