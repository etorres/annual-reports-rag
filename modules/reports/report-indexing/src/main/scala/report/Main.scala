package es.eriktorr
package report

import embedding.db.{ElasticClient, VectorStoreBuilder}
import report.api.{ReportInfoLoader, ReportLoader}
import report.application.ReportLoaderConfig
import report.domain.ReportLoaderService

import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val reportsPath = os.pwd / "modules" / "reports"
    val samplePath = reportsPath / "sample-reports" / "src" / "main" / "resources" / "samples"
    val subsetPath = samplePath / "subset.json"
    val config = ReportLoaderConfig.localContainer
    (for
      logger <- Resource.eval(Slf4jLogger.create[IO])
      given Logger[IO] = logger
      elasticClient <- ElasticClient.resource(config.elasticConfig)
      vectorStoreBuilder = VectorStoreBuilder(elasticClient)
      reportInfoLoader = ReportInfoLoader(subsetPath)
      reportLoader = ReportLoader(reportInfoLoader, vectorStoreBuilder)
      reportLoaderService = ReportLoaderService(reportLoader)
    yield (logger, reportLoaderService)).use:
      case (logger, reportLoaderService) =>
        for
          _ <- logger.info(s"Loading reports from $samplePath...")
          numReportsLoaded <- reportLoaderService.loadReportsFrom(samplePath)
          _ <- logger.info(s"$numReportsLoaded reports loaded")
        yield ExitCode.Success
