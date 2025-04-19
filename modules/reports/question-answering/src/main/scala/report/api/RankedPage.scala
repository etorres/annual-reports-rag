package es.eriktorr
package report.api

import cats.Show

final case class RankedPage(page: Page, llmScore: Double, vectorScore: Double):
  def score: Double = .7d * llmScore + .3d * vectorScore

object RankedPage:
  given Show[RankedPage] =
    Show.show: x =>
      val page = x.page
      s"${page.filename} page ${page.page} with score: llm=${x.llmScore}, vector=${x.vectorScore}, weighted=${x.score}"
