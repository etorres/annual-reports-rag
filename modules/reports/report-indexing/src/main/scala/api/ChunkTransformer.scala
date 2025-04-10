package es.eriktorr
package api

import api.TextSegmentExtensions.put

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.{TextSegment, TextSegmentTransformer}

final class ChunkTransformer(document: Document, indexName: String) extends TextSegmentTransformer:
  private val documentFilename = maybeValue(DocumentMetadata.Filename)
  private val documentSummary = maybeValue(DocumentMetadata.Summary)
  private val documentTitle = maybeValue(DocumentMetadata.Title)

  private def maybeValue(key: DocumentMetadata) =
    val documentMetadata = document.metadata()
    if documentMetadata.containsKey(key.name)
    then Some(documentMetadata.getString(key.name))
    else None

  override def transform(textSegment: TextSegment): TextSegment =
    textSegment
      .put(DocumentMetadata.IndexName, Some(indexName))
      .put(DocumentMetadata.Filename, documentFilename)
      .put(DocumentMetadata.Summary, documentSummary)
      .put(DocumentMetadata.Title, documentTitle)
