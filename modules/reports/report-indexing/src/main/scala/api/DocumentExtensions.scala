package es.eriktorr
package api

import dev.langchain4j.data.document.Document

import scala.util.chaining.*

object DocumentExtensions:
  extension (self: Document)
    def metadataAsString(reportMetadata: ReportMetadata): String =
      self.metadata().getString(reportMetadata.name)
    def put(reportMetadata: ReportMetadata, value: String): Document =
      self.tap(_.metadata().put(reportMetadata.name, value))
