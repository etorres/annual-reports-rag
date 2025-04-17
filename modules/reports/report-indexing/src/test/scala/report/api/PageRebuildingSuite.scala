package es.eriktorr
package report.api

import cats.effect.IO
import munit.CatsEffectSuite

final class PageRebuildingSuite extends CatsEffectSuite:
  test("should rebuild a page from chunks and remove the overlap"):
    IO.unit.map(fail("not implemented"))
