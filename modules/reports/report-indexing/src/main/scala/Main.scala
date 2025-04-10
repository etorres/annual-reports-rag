package es.eriktorr

import api.OllamaModel
import application.ReportLoaderConfig
import domain.ReportLoaderService

object Main:
  @main
  def run(): Unit =
    val reportPath = os.pwd / "modules" / "reports"
    val samplesPath = reportPath / "sample-reports" / "src" / "main" / "resources" / "small"
    val config = ReportLoaderConfig.singleProcessor(OllamaModel.Gemma3)
    ReportLoaderService(config).loadReports(samplesPath)
    sys.exit(0)
