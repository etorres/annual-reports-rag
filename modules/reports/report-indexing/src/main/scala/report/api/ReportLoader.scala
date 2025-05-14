package es.eriktorr
package report.api

import common.api.DocumentMetadata
import common.io.{FileType, PathFinder, TempDir}
import common.security.MessageDigest
import embedding.db.{VectorStore, VectorStoreBuilder}
import report.api.DocumentExtensions.metadataAsString

import cats.implicits.catsSyntaxParallelTraverse1
import cats.effect.{IO, Resource}
import cats.implicits.toTraverseOps
import dev.langchain4j.data.document.Document
import org.apache.commons.io.FilenameUtils
import org.typelevel.log4cats.Logger

final class ReportLoader(
    reportInfoLoader: ReportInfoLoader,
    vectorStoreBuilder: VectorStoreBuilder,
)(using logger: Logger[IO]):
  def loadReportsFrom(dir: os.Path): IO[Int] =
    for
      paths <- PathFinder.find(dir = dir, FileType.PDF)
      _ <- logger.info(s"Loading ${paths.length} reports...")
      _ <- paths.parTraverse: path =>
        for
          _ <- logger.info(s"Loading report ${path.last}...")
          document <- loadDocument(path)
          _ <- loadPages(path, document)
        yield ()
    yield paths.length

  private def loadDocument(path: os.Path) =
    for
      fileChecksum <- MessageDigest.checksum(path)
      reportInfo <- reportInfoLoader
        .findReportInfoBy(fileChecksum, simple = true)
        .getOrRaise(IllegalArgumentException(s"No report info found for: $path"))
      document <- PdfDocument.loadDocument(path)
      enrichedDocument = ReportTransformer.transform(document, reportInfo)
    yield enrichedDocument

  private def loadPages(path: os.Path, document: Document) =
    (for
      tempDir <- TempDir.make("report_loader")
      pdf <- PdfDocument.parsePdf(path)
    yield tempDir -> pdf).use:
      case (tempDir, pdf) =>
        for
          numberedPdfPages <- PdfDocument.toPages(pdf, path.last)
          basename = FilenameUtils.getBaseName(path.last)
          indexName = indexNameFrom(document)
          vectorStore = vectorStoreBuilder.impl(indexName)
          _ <- numberedPdfPages.traverse:
            case PdfPage(pdfPage, pageNum) =>
              Resource
                .fromAutoCloseable:
                  IO.pure(pdfPage)
                .use: handledPdfPage =>
                  val tempPath = tempDir / s"$basename-page-$pageNum.pdf"
                  for
                    _ <- IO.blocking:
                      handledPdfPage.save(tempPath.toString)
                    maybePage <- loadPage(tempPath, pageNum, document).value
                    _ <- maybePage.traverse: page =>
                      storeVectors(page, vectorStore)
                  yield ()
          _ <- vectorStore.refreshIndex()
        yield ()

  private def loadPage(path: os.Path, pageNum: Int, parent: Document) =
    for
      page <- PdfDocument.loadPage(path)
      enrichedPage = ReportTransformer.transform(page, pageNum, parent)
    yield enrichedPage

  private def storeVectors(page: Document, vectorStore: VectorStore) =
    val textSegments = PdfDocument.toTextSegments(page)
    val enrichedTextSegments = textSegments.zipWithIndex.map:
      case (textSegment, idx) =>
        ChunkTransformer.transform(idx + 1, page, textSegment)
    vectorStore.add(enrichedTextSegments)

  private def indexNameFrom(document: Document) =
    document.metadataAsString(DocumentMetadata.Sha1FileChecksum)
