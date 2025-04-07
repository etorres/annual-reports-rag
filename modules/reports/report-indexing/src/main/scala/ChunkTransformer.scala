package es.eriktorr

import TextSegmentExtensions.put

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.{TextSegment, TextSegmentTransformer}

final class ChunkTransformer(document: Document, indexName: String) extends TextSegmentTransformer:
  private val documentFilename = maybeValue(ReportMetadata.Filename)
  private val documentSummary = maybeValue(ReportMetadata.Summary)
  private val documentTitle = maybeValue(ReportMetadata.Title)

  private def maybeValue(key: ReportMetadata) =
    val documentMetadata = document.metadata()
    if documentMetadata.containsKey(key.name)
    then Some(documentMetadata.getString(key.name))
    else None

  override def transform(textSegment: TextSegment): TextSegment =
    textSegment
      .put(ReportMetadata.IndexName, Some(indexName))
      .put(ReportMetadata.Filename, documentFilename)
      .put(ReportMetadata.Summary, documentSummary)
      .put(ReportMetadata.Title, documentTitle)
