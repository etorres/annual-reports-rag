package es.eriktorr
package report.api

import cats.data.OptionT
import cats.effect.{IO, Resource, ResourceIO}
import cats.implicits.*
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.document.{BlankDocumentException, Document}
import dev.langchain4j.data.segment.TextSegment
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.typelevel.log4cats.Logger

import scala.jdk.CollectionConverters.given

object PdfDocument:
  def loadDocument(path: os.Path): IO[Document] =
    IO.blocking:
      FileSystemDocumentLoader.loadDocument(path.toNIO, documentParser)

  def loadPage(path: os.Path)(using logger: Logger[IO]): OptionT[IO, Document] =
    OptionT:
      IO.blocking:
        FileSystemDocumentLoader.loadDocument(path.toNIO, pageParser).some
      .handleErrorWith:
          case error: BlankDocumentException =>
            logger.info(s"Ignoring blank page: ${path.last}") *> IO.none
          case other => IO.raiseError(other)

  def parsePdf(path: os.Path): ResourceIO[PDDocument] =
    Resource.fromAutoCloseable:
      IO.blocking:
        PDDocument.load(path.toNIO.toFile)

  def toPages(pdf: PDDocument, name: String): IO[List[PdfPage]] =
    for
      pages <- IO.blocking:
        Splitter().split(pdf).listIterator().asScala.toList
      numberedPages = pages.zipWithIndex.map:
        case (page, idx) => PdfPage(page, idx + 1)
      _ <- IO
        .pure(numberedPages.length == pdf.getNumberOfPages)
        .ifM(
          ifTrue = IO.unit,
          ifFalse = IO.raiseError(
            IllegalArgumentException(
              s"Number of pages of $name differ after splitting: ${pdf.getNumberOfPages}/${numberedPages.length}",
            ),
          ),
        )
    yield numberedPages

  def toTextSegments(document: Document): List[TextSegment] =
    documentSplitter.split(document).asScala.toList

  private lazy val documentParser = ApachePdfBoxDocumentParser(true)
  private lazy val documentSplitter = DocumentSplitters.recursive(300, 50) // TODO: add tokenizer
  private lazy val pageParser = ApachePdfBoxDocumentParser(false)
