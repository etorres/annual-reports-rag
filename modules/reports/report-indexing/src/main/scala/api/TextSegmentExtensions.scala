package es.eriktorr
package api

import DocumentExtensions.metadataAsString

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment

import scala.util.chaining.*

object TextSegmentExtensions:
  extension (self: TextSegment)
    def copy(documentMetadata: DocumentMetadata, document: Document): TextSegment =
      self
        .metadata()
        .put(
          documentMetadata.name,
          document.metadataAsString(documentMetadata),
        )
      self
    def put(documentMetadata: DocumentMetadata, maybeValue: Option[String]): TextSegment =
      maybeValue
        .map: value =>
          self.tap(_.metadata().put(documentMetadata.name, value))
        .getOrElse(self)
