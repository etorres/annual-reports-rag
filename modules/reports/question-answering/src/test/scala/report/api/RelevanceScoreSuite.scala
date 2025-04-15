package es.eriktorr
package report.api

import munit.CatsEffectSuite

final class RelevanceScoreSuite extends CatsEffectSuite:
  test("should extract the relevance score from a text"):
    RelevanceScore.from("**Relevance Score:** 0.2").assertEquals(.2d)

  test("should extract the relevance score from a multi-line text"):
    RelevanceScore
      .from("**1. Reasoning:**\n\nNone\n\n**2. Relevance Score:**\n\n0.1")
      .assertEquals(.1d)
