package es.eriktorr
package report

import embedding.db.{ElasticClient, VectorStoreBuilder}
import ollama.api.{HttpClient, OllamaApiClient, OllamaModel}
import report.api.{ReportLoader, TextSummarizer}
import report.application.ReportLoaderConfig
import report.domain.ReportLoaderService

import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.DurationInt

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val reportsPath = os.pwd / "modules" / "reports"
    val samplePath = reportsPath / "sample-reports" / "src" / "main" / "resources" / "samples"
    val config = ReportLoaderConfig.localContainerFor(1, OllamaModel.Gemma3)
    val verbose = false
    (for
      logger <- Resource.eval(Slf4jLogger.create[IO])
      given Logger[IO] = logger
      elasticClient <- ElasticClient.resource(config.elasticConfig)
      httpClient <- HttpClient.resource(30.seconds)
      ollamaApiClient = OllamaApiClient(httpClient, config.ollamaConfig)
      vectorStoreBuilder = VectorStoreBuilder(elasticClient)
      textSummarizer = TextSummarizer.impl(config.ollamaConfig, verbose)
      reportLoader = ReportLoader(config.maximumParallelism, textSummarizer, vectorStoreBuilder)
      reportLoaderService = ReportLoaderService(reportLoader)
    yield (logger, ollamaApiClient, reportLoaderService)).use:
      case (logger, ollamaApiClient, reportLoaderService) =>
        for
          _ <- ollamaApiClient.preload()
          _ <- logger.info(s"Loading reports from $samplePath...")
          numReportsLoaded <- reportLoaderService.loadReportsFrom(samplePath)
          _ <- logger.info(s"$numReportsLoaded reports loaded")
        yield ExitCode.Success
