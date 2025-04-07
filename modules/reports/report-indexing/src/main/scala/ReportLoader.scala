package es.eriktorr

import DocumentExtensions.metadataAsString
import TextSegmentExtensions.copy

import com.typesafe.scalalogging.StrictLogging
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.document.{
  Document,
  DocumentParser,
  DocumentSplitter,
  DocumentTransformer,
}
import dev.langchain4j.data.segment.TextSegment

import java.util.Locale
import java.util.concurrent.Executors
import scala.collection.parallel.CollectionConverters.given
import scala.collection.parallel.ExecutionContextTaskSupport
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.given

final class ReportLoader(
    config: OllamaConfig,
    summaryVectorStore: VectorStore,
    vectorStoreBuilder: VectorStoreBuilder,
    verbose: Boolean = false,
) extends StrictLogging:
  def allReportsFrom(dir: os.Path): Unit =
    parallelPathsFrom(dir).foreach: path =>
      val document = loadDocument(
        path,
        ApachePdfBoxDocumentParser(true),
        ReportTransformer.impl(config, verbose),
      )
      storeSummary(document)
      storeContent(document, DocumentSplitters.recursive(300, 0) /* TODO: add tokenizer */ )

  private def parallelPathsFrom(dir: os.Path) =
    val paths = os.walk(path = dir, skip = _.ext.toLowerCase(Locale.US) != "pdf", maxDepth = 1)
    logger.info(s"Loading ${paths.length} reports from: $dir")
    enableParallelProcessing(paths)

  private def enableParallelProcessing(paths: IndexedSeq[os.Path]) =
    val parallelPaths = paths.par
    parallelPaths.tasksupport = taskSupportFrom(config.processors)
    parallelPaths

  private def taskSupportFrom(processors: Int) =
    ExecutionContextTaskSupport(
      ExecutionContext.fromExecutor(
        if processors > 1 then Executors.newFixedThreadPool(processors)
        else Executors.newSingleThreadExecutor(),
      ),
    )

  private def loadDocument(
      path: os.Path,
      documentParser: DocumentParser,
      documentTransformer: DocumentTransformer,
  ) =
    logger.info(s"Loading report ${path.last}")
    val document = FileSystemDocumentLoader.loadDocument(path.toNIO, documentParser)
    documentTransformer.transform(document)

  private def storeSummary(document: Document): Unit =
    val summary = TextSegment
      .from(document.metadataAsString(ReportMetadata.Summary))
      .copy(ReportMetadata.IndexName, document)
    summaryVectorStore.add(summary)

  private def storeContent(document: Document, documentSplitter: DocumentSplitter): Unit =
    val indexName = document.metadataAsString(ReportMetadata.IndexName)
    val textSegments = documentSplitter.split(document).asScala.toList
    val textSegmentTransformer = ChunkTransformer(document, indexName)
    val enrichedTextSegments = textSegments.map(textSegmentTransformer.transform)
    vectorStoreBuilder.impl(indexName).add(enrichedTextSegments)
