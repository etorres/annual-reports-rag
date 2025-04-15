package es.eriktorr
package report.api

import ollama.api.OllamaModel

import munit.FunSuite

final class SummaryCleanerSuite extends FunSuite:
  summaryPath.test("should clean a summary removing the header and the suggested actions"): path =>
    testWith(path, "40b5cfe0d7bbf59e186492bfbe1b5002d44af332")

  summaryPath.test("should clean a summary removing the header and the help offer"): path =>
    testWith(path, "58a5f9f5c83159e63602b0b1dd27c27fb945c0e9")

  summaryPath.test("should clean a summary with an special char in the header"): path =>
    testWith(path, "3696c1b29566acc1eafc704ee5737fb3ae6f3d1d")

  summaryPath.test("should clean a summary where the suggested actions has no delimiter"): path =>
    testWith(path, "d81bbc64a4160b9946fea7a895f80e6201f52f27")

  summaryPath.test("should clean a summary also with header and actions"): path =>
    testWith(path, "ea0757d27fa67cd347d9f046b939a911f5c9a08d")

  private def testWith(path: os.Path, filename: String): Unit =
    val summary = os.read(path / "gemma3" / s"$filename.txt")
    val expected = os.read(path / "gemma3" / s"$filename-clean.txt")
    val obtained = SummaryCleaner.impl(OllamaModel.Gemma3).clean(summary)
    assertEquals(obtained, expected, "clean summary")

  private lazy val summaryPath = FunFixture[os.Path](
    setup = _ =>
      val reportsPath = os.pwd / "modules" / "reports"
      reportsPath / "report-indexing" / "src" / "test" / "resources" / "summary"
    ,
    teardown = _ => (),
  )
