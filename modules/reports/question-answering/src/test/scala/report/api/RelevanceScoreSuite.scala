package es.eriktorr
package report.api

import munit.CatsEffectSuite

final class RelevanceScoreSuite extends CatsEffectSuite:
  test("should extract the relevance score from an unnumbered list"):
    RelevanceScore
      .from("Okay:\n\n**Relevance Score:** 0.2**\n\nAdditional Guidance")
      .assertEquals(.2d)

  test("should extract the relevance score from a numbered list"):
    RelevanceScore
      .from("**1. Reasoning:**\n\nNone\n\n**2. Relevance Score:**\n\n0.1")
      .assertEquals(.1d)

  test("should extract the relevance score from a highlighted block"):
    RelevanceScore
      .from(
        "**Reasoning:** The provided text block\n\n**Relevance Score: 0.7**\n\nAdditional Guidance:**",
      )
      .assertEquals(.7d)
