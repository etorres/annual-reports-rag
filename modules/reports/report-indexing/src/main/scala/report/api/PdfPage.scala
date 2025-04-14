package es.eriktorr
package report.api

import org.apache.pdfbox.pdmodel.PDDocument

final case class PdfPage(page: PDDocument, pageNum: Int)
