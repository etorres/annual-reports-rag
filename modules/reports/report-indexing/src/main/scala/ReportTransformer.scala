package es.eriktorr

import DocumentExtensions.*
import LangChain4jUtils.variablesFrom

import com.typesafe.scalalogging.StrictLogging
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.{Document, DocumentTransformer}
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.input.PromptTemplate

final class ReportTransformer(chatModel: ChatLanguageModel)
    extends DocumentTransformer
    with StrictLogging:
  private val promptTemplate = PromptTemplate.from(
    FileSystemDocumentLoader
      .loadDocument(
        LangChain4jUtils.pathTo("prompt_templates/summarize.txt"),
        TextDocumentParser(),
      )
      .text(),
  )

  override def transform(document: Document): Document =
    val filename = document.metadataAsString(ReportMetadata.Filename)
    document
      .put(ReportMetadata.IndexName, indexNameFrom(filename))
      .put(ReportMetadata.Summary, summaryFrom(document, filename))

  private def indexNameFrom(filename: String) = filename.replaceAll("(?<!^)[.].*", "")

  private def summaryFrom(document: Document, filename: String) =
    logger.info(s"Summarizing: $filename, it would take several minutes...")
    val summary = chatModel.chat(
      promptTemplate.apply(variablesFrom("content" -> document.text())).text(),
    )
    clean(summary, filename)

  private def clean(summary: String, filename: String) =
    println(s" >> $filename, summary: \n $summary") // TODO
    summary // TODO
