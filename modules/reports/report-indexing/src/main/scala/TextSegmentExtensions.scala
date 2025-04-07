package es.eriktorr

import DocumentExtensions.metadataAsString

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment

import scala.util.chaining.*

object TextSegmentExtensions:
  extension (self: TextSegment)
    def copy(reportMetadata: ReportMetadata, document: Document): TextSegment =
      self
        .metadata()
        .put(
          reportMetadata.name,
          document.metadataAsString(reportMetadata),
        )
      self
    def put(reportMetadata: ReportMetadata, maybeValue: Option[String]): TextSegment =
      maybeValue
        .map: value =>
          self.tap(_.metadata().put(reportMetadata.name, value))
        .getOrElse(self)
