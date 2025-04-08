package es.eriktorr
package domain

import api.{OllamaModel, ReportMetadata}
import application.ReportLoaderConfig
import db.VectorStoreRouter

import munit.FunSuite

import scala.concurrent.duration.{Duration, DurationInt}

final class ReportLoaderServiceSuite extends FunSuite:
  testCases.test("should load reports using Gemma3"): path =>
    val config = ReportLoaderConfig.singleProcessor(OllamaModel.Gemma3)
    testWith(config, path)

  test("should load reports using Mistral"):
    fail("not implemented")

  test("should load reports using Phi4"):
    fail("not implemented")

  private def testWith(config: ReportLoaderConfig, path: os.Path): Unit =
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
    assertEquals(
      obtained,
      Some("ea0757d27fa67cd347d9f046b939a911f5c9a08d"),
      s"index name using ${config.ollamaConfig.model.name}",
    )

  override def munitTimeout: Duration = 5.minutes

  private lazy val testCases = FunFixture[os.Path](
    setup = _ =>
      val reportPath = os.pwd / "modules" / "reports"
      reportPath / "sample-reports" / "src" / "main" / "resources" / "one"
    ,
    teardown = _ => (),
  )
