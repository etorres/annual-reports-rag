package es.eriktorr

import com.typesafe.scalalogging.StrictLogging
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.ollama.OllamaChatModel

import java.nio.file.Path as JPath
import java.time.Duration as JDuration
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
    val paths = os.walk(path = dir, skip = _.ext.toLowerCase(Locale.US) != "pdf", maxDepth = 1)
    logger.info(s"Loading ${paths.length} reports from: $dir")
    val parallelPaths = paths.par
    parallelPaths.tasksupport = taskSupportFrom(config.processors)
    val documentParser = ApachePdfBoxDocumentParser(true)
    val documentTransformer = ReportTransformer(chatModelFrom(config, verbose))
    val documentSplitter = DocumentSplitters.recursive(300, 0) // TODO: add tokenizer
    parallelPaths.foreach: path =>
      val jPath = path.toNIO
      val indexName = fileWithoutExtFrom(jPath)
      logger.info(s"Loading report ${jPath.getFileName} into index: $indexName")
      val document = FileSystemDocumentLoader.loadDocument(jPath, documentParser)
      val enrichedDocument = documentTransformer.transform(document)
      val textSegments = documentSplitter.split(enrichedDocument).asScala.toList
      val textSegmentTransformer = ChunkTransformer(enrichedDocument, indexName)
      val enrichedTextSegments = textSegments.map(textSegmentTransformer.transform)
      summaryVectorStore.add(
        TextSegment.from(document.metadata().getString(ReportMetadata.Summary.name)),
      )
      vectorStoreBuilder.impl(indexName).add(enrichedTextSegments)

  private def fileWithoutExtFrom(path: JPath) =
    path.getFileName.toString.replaceAll("(?<!^)[.].*", "")

  private def taskSupportFrom(processors: Int) =
    ExecutionContextTaskSupport(
      ExecutionContext.fromExecutor(
        if processors > 1 then Executors.newFixedThreadPool(processors)
        else Executors.newSingleThreadExecutor(),
      ),
    )

  private def chatModelFrom(config: OllamaConfig, verbose: Boolean) =
    val responseFormat = config.model match
      case OllamaModel.Llama3_2 | OllamaModel.TinyLlama => ResponseFormat.JSON
      case _ => ResponseFormat.TEXT
    OllamaChatModel
      .builder()
      .baseUrl(config.baseUrl)
      .logRequests(verbose)
      .logResponses(verbose)
      .maxRetries(3)
      .modelName(config.model.name)
      .repeatPenalty(0.8d)
      .responseFormat(responseFormat)
      .temperature(0.2d)
      .timeout(JDuration.ofMinutes(10L))
      .topK(40)
      .topP(0.9d)
      .build()
