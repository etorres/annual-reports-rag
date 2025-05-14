package es.eriktorr
package report.api

import common.api.DocumentMetadata
import report.api.DocumentExtensions.{copy, metadataAsString, put}

import dev.langchain4j.data.document.Document

import scala.annotation.tailrec

object ReportTransformer:
  def transform(document: Document, reportInfo: ReportInfo): Document =
    val filename = document.metadataAsString("file_name")
    document
      .put(DocumentMetadata.CompanyName, reportInfo.companyName)
      .put(DocumentMetadata.Filename, filename)
      .put(DocumentMetadata.Sha1FileChecksum, reportInfo.sha1)

  def transform(page: Document, pageNum: Int, parent: Document): Document =
    @tailrec
    def copy(accumulator: Document, keys: List[DocumentMetadata]): Document =
      keys match
        case Nil => accumulator
        case ::(head, next) => copy(accumulator.copy(head, parent), next)
    end copy
    copy(page, DocumentMetadata.editionFields)
      .put(DocumentMetadata.Page, pageNum.toString)
