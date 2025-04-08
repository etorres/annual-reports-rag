package es.eriktorr

import application.ReportLoaderConfig
import domain.ReportLoaderService

object Main:
  @main
  def run(): Unit =
    val reportPath = os.pwd / "modules" / "reports"
    val samplesPath = reportPath / "sample-reports" / "src" / "main" / "resources" / "samples"
    val config = ReportLoaderConfig.singleProcessor(OllamaModel.Gemma3)
    ReportLoaderService(config).loadReports(samplesPath)
    sys.exit(0)
