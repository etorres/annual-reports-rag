package es.eriktorr
package domain

import api.ReportMetadata
import application.ReportLoaderConfig
import db.VectorStoreRouter

import munit.FunSuite

import scala.concurrent.duration.{Duration, DurationInt}

final class ReportLoaderServiceTest extends FunSuite:
  test("should load reports using Gemma3"):
    val reportPath = os.pwd / "modules" / "reports"
    val path = reportPath / "sample-reports" / "src" / "main" / "resources" / "one"
    val config = ReportLoaderConfig.singleProcessor(OllamaModel.Gemma3)
    ReportLoaderService(config).loadReports(path)
    val vectorStoreRouter = VectorStoreRouter.impl(
      config.elasticConfig,
      "summary",
      ReportMetadata.IndexName.name,
    )
    vectorStoreRouter.refreshIndex()
    val obtained = vectorStoreRouter.indexNameFor(
      "Who is the Board Chairman in the company \"Canadian Banc Corp.\"?",
    )
    assertEquals(obtained, Some("ea0757d27fa67cd347d9f046b939a911f5c9a08d"), "index name")

  test("should load reports using Mistral"):
    fail("not implemented")

  test("should load reports using Phi4"):
    fail("not implemented")

  override def munitTimeout: Duration = 5.minutes
