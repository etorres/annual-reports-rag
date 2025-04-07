package es.eriktorr

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
    val filename = document.metadata().getString(ReportMetadata.Filename.name)
    // summarizing the document
    logger.info(s"Summarizing: $filename, it would take several minutes...")
    val summary = chatModel.chat(
      promptTemplate.apply(variablesFrom("content" -> document.text())).text(),
    )
    // TODO
    println(s" >> $filename, summary: \n $summary")
    // TODO: Clean summary
    // TODO
    document.metadata().put(ReportMetadata.Summary.name, summary)
    logger.info(s"Transformed: $filename")
    document
