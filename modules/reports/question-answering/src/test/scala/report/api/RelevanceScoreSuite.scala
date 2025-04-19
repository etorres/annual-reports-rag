package es.eriktorr
package report.api

import report.api.RelevanceScoreSuite.*

import munit.CatsEffectSuite

final class RelevanceScoreSuite extends CatsEffectSuite:
  test("should extract the relevance score from a given text (1)"):
    testWith(testCase1)

  test("should extract the relevance score from a given text (2)"):
    testWith(testCase2)

  test("should extract the relevance score from a given text (3)"):
    testWith(testCase3)

  test("should extract the relevance score from a given text (4)"):
    testWith(testCase4)

  test("should extract the relevance score from a given text (5)"):
    testWith(testCase5)

  test("should extract the relevance score from a given text (6)"):
    testWith(testCase6)

  private def testWith(testCase: TestCase) =
    RelevanceScore
      .from(testCase.text)
      .assertEquals(testCase.score)

object RelevanceScoreSuite:
  final private case class TestCase(text: String, score: Double)

  private val testCase1 = TestCase(
    "Okay:\n\n**Relevance Score:** 0.2**\n\nAdditional Guidance",
    .2d,
  )

  private val testCase2 = TestCase(
    "**1. Reasoning:**\n\nNone\n\n**2. Relevance Score:**\n\n0.1",
    .1d,
  )

  private val testCase3 = TestCase(
    "**Reasoning:** The provided text block\n\n**Relevance Score: 0.7**\n\nAdditional Guidance:**",
    .7d,
  )

  private val testCase4 = TestCase(
    "Chairman’s identity.\n\n**2. Relevance Score (0 to 1):** 0.2\n\n**3. Additional Guidance:**",
    .2d,
  )

  private val testCase5 = TestCase(
    "based on this text.\n\nRelevance Score: 0.1\n\nAdditional Guidance:",
    .1d,
  )

  private val testCase6 = TestCase(
    "the company’s cash flow activities.\n\n**Relevance Score:** 0\n\n**Additional Guidance:**",
    0d,
  )
