package es.eriktorr
package api

import munit.FunSuite

final class SummaryCleanerSuite extends FunSuite:
  test("should clean a summary produced by Gemma3"):
    val reportPath = os.pwd / "modules" / "reports"
    val path = reportPath / "report-indexing" / "src" / "test" / "resources" / "summary"
    val summary = os.read(path / "gemma3" / "ea0757d27fa67cd347d9f046b939a911f5c9a08d.txt")
    val expected = os.read(path / "gemma3" / "ea0757d27fa67cd347d9f046b939a911f5c9a08d-clean.txt")
    val obtained = SummaryCleaner.impl(OllamaModel.Gemma3).clean(summary)
    assertEquals(obtained, expected, "summary without notes")
