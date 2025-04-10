package es.eriktorr
package api

import api.LangChain4jUtils.{pathTo, variablesFrom}
import api.OllamaChatModelBuilderExtensions.responseFormat
import application.OllamaConfig

import com.typesafe.scalalogging.StrictLogging
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.ollama.OllamaChatModel

import java.time.Duration as JDuration

final class TextSummarizer(config: OllamaConfig, verbose: Boolean) extends StrictLogging:
  private val summaryCleaner = SummaryCleaner.impl(config.model)

  def summaryFrom(document: Document, filename: String): String =
    logger.info(s"Summarizing: $filename, it would take several minutes...")
    val summary = chatModel.chat(
      promptTemplate.apply(variablesFrom("content" -> document.text())).text(),
    )
    summaryCleaner.clean(summary)

  private lazy val chatModel =
    OllamaChatModel
      .builder()
      .baseUrl(config.baseUrl)
      .logRequests(verbose)
      .logResponses(verbose)
      .maxRetries(3)
      .modelName(config.model.name)
      .repeatPenalty(0.8d)
      .responseFormat(config.model)
      .temperature(0.2d)
      .timeout(JDuration.ofMinutes(10L))
      .topK(40)
      .topP(0.9d)
      .build()

  private lazy val promptTemplate = PromptTemplate.from(
    FileSystemDocumentLoader
      .loadDocument(
        pathTo("prompt_templates/summarize.txt"),
        TextDocumentParser(),
      )
      .text(),
  )
