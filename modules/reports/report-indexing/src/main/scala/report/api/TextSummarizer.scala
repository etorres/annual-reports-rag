package es.eriktorr
package report.api

import common.api.LangChain4jUtils.pathTo
import ollama.api.OllamaChatModelBuilderExtensions.responseFormat
import ollama.application.OllamaConfig

import cats.effect.IO
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.message.{SystemMessage, UserMessage}
import dev.langchain4j.model.ollama.OllamaChatModel
import org.typelevel.log4cats.Logger

import java.time.Duration as JDuration

trait TextSummarizer:
  def summaryFrom(document: Document, filename: String): IO[String]

object TextSummarizer:
  def impl(config: OllamaConfig, verbose: Boolean)(using logger: Logger[IO]): TextSummarizer =
    val chatModel =
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
    val systemMessage = SystemMessage.from(
      FileSystemDocumentLoader
        .loadDocument(
          pathTo("prompt_templates/summarize.txt"),
          TextDocumentParser(),
        )
        .text(),
    )
    val summaryCleaner = SummaryCleaner.impl(config.model)
    (document: Document, filename: String) =>
      for
        _ <- logger.info(s"Summarizing: $filename, it would take several minutes...")
        summary <- IO.blocking:
          chatModel
            .chat(
              systemMessage,
              UserMessage.from(
                s"""Here is the text:
                   |${document.text()}""".stripMargin,
              ),
            )
            .aiMessage()
            .text()
        cleanSummary = summaryCleaner.clean(summary)
      yield summary
