package es.eriktorr
package api

import dev.langchain4j.data.document.Document

import scala.util.chaining.*

object DocumentExtensions:
  extension (self: Document)
    def metadataAsString(key: String): String =
      self.metadata().getString(key)
    def metadataAsString(documentMetadata: DocumentMetadata): String =
      metadataAsString(documentMetadata.name)
    def put(documentMetadata: DocumentMetadata, value: String): Document =
      self.tap(_.metadata().put(documentMetadata.name, value))
