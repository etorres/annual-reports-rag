package es.eriktorr
package api

import api.DocumentExtensions.{metadataAsString, put}
import application.OllamaConfig

import dev.langchain4j.data.document.{Document, DocumentTransformer}

final class ReportTransformer(textSummarizer: TextSummarizer) extends DocumentTransformer:
  override def transform(document: Document): Document =
    val filename = document.metadataAsString(ReportMetadata.Filename)
    document
      .put(ReportMetadata.IndexName, indexNameFrom(filename))
      .put(ReportMetadata.Summary, textSummarizer.summaryFrom(document, filename))

  private def indexNameFrom(filename: String) = filename.replaceAll("(?<!^)[.].*", "")

object ReportTransformer:
  def impl(config: OllamaConfig, verbose: Boolean): ReportTransformer =
    val textSummarizer = TextSummarizer(config, verbose)
    ReportTransformer(textSummarizer)
