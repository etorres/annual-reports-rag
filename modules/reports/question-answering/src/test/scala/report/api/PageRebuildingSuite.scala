package es.eriktorr
package report.api

import embedding.db.DocumentResult
import report.api.PageRebuildingSuite.{loadSample, TestCase}

import munit.FunSuite
import org.apache.commons.io.FilenameUtils

final class PageRebuildingSuite extends FunSuite:
  test("should rebuild a page from chunks and remove the overlap (sample 1)"):
    testWith(loadSample(1))

  test("should rebuild a page from chunks and remove the overlap (sample 2)"):
    testWith(loadSample(2))

  test("should rebuild a page from chunks and remove the overlap (sample 3)"):
    testWith(loadSample(3))

  private def testWith(testCase: TestCase): Unit =
    val obtained = PageRebuilding.rebuild(testCase.documentResults)
    assertEquals(obtained.sortBy(_.page), testCase.expected.sortBy(_.page))

object PageRebuildingSuite:
  final private case class TestCase(documentResults: List[DocumentResult], expected: List[Page])

  private val reportsPath = os.pwd / "modules" / "reports"
  private val samplePath = reportsPath / "sample-reports" / "src" / "test" / "resources" / "simple"

  private def loadSample(n: Int) =
    val basename = s"sample$n"
    val filename = s"$basename.pdf"
    val pages = os.walk(path = samplePath / basename / "chunks", maxDepth = 1).map(_.last).toList
    val documentResults = pages.flatMap: page =>
      val pageNum = page.substring(4).toInt
      os.walk(path = samplePath / basename / "chunks" / page, maxDepth = 1)
        .map(_.last)
        .toList
        .map: chunk =>
          val chunkNum = FilenameUtils.getBaseName(chunk).substring(5).toInt
          val text = os.read(samplePath / basename / "chunks" / page / chunk)
          DocumentResult(chunkNum, filename, pageNum, text)
    val expected = pages.map: page =>
      val pageNum = page.substring(4).toInt
      val text = os.read(samplePath / basename / "pages" / s"$page.txt")
      Page(filename, pageNum, text)
    // TODO
//    println(s" >> PAGES: $pages")
//    documentResults.foreach(x =>
//      println(s" >> DOC: ${x.filename}, ${x.page}, ${x.chunk}, ${x.text}"),
//    )
//    expected.foreach(x => println(s" >> EXPECTED: ${x.filename}, ${x.page}, ${x.text}"))
    // TODO
    TestCase(documentResults, expected) // TODO
