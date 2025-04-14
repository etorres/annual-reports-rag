package es.eriktorr
package report.api

import common.api.DocumentMetadata
import report.api.DocumentExtensions.maybeMetadataAsString

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment

import scala.util.chaining.*

object TextSegmentExtensions:
  extension (self: TextSegment)
    def copy(documentMetadata: DocumentMetadata, document: Document): TextSegment =
      document
        .maybeMetadataAsString(documentMetadata)
        .map: value =>
          put(documentMetadata, value)
        .getOrElse(self)
    def put(documentMetadata: DocumentMetadata, value: String): TextSegment =
      self.tap(_.metadata().put(documentMetadata.name, value))
