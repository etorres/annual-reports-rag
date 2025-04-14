package es.eriktorr
package common.security

import common.io.{FileType, PathFinder}

import cats.effect.Resource
import cats.implicits.*
import munit.{AnyFixture, CatsEffectSuite}

final class MessageDigestSuite extends CatsEffectSuite:
  test("should generate the SHA-1 checksum for a file"):
    (for
      paths <- PathFinder.find(dir = testCases(), FileType.PDF)
      obtained <- paths.parTraverse(MessageDigest.checksum)
      expected = paths.map: path =>
        path.last.replaceAll("(?<!^)[.].*", "")
    yield (obtained, expected)).map:
      case (obtained, expected) =>
        assertEquals(obtained, expected, "checksum matches")

  private lazy val testCases = ResourceSuiteLocalFixture(
    "test-cases",
    Resource.pure {
      val reportPath = os.pwd / "modules" / "reports"
      reportPath / "sample-reports" / "src" / "main" / "resources" / "samples"
    },
  )

  override def munitFixtures: Seq[AnyFixture[?]] = List(testCases)
