package es.eriktorr
package report.api

import common.api.DocumentMetadata

import dev.langchain4j.data.document.Document

import scala.util.chaining.*

object DocumentExtensions:
  extension (self: Document)
    def copy(documentMetadata: DocumentMetadata, document: Document): Document =
      document
        .maybeMetadataAsString(documentMetadata)
        .map: value =>
          put(documentMetadata, value)
        .getOrElse(self)
    def metadataAsString(key: String): String =
      self.metadata().getString(key)
    def metadataAsString(documentMetadata: DocumentMetadata): String =
      metadataAsString(documentMetadata.name)
    def maybeMetadataAsString(documentMetadata: DocumentMetadata): Option[String] =
      if self.metadata().containsKey(documentMetadata.name) then
        Some(self.metadataAsString(documentMetadata))
      else None
    def put(documentMetadata: DocumentMetadata, value: String): Document =
      self.tap(_.metadata().put(documentMetadata.name, value))
