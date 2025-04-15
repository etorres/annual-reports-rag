package es.eriktorr
package common

import munit.FunSuite

final class CanonicalStringSuite extends FunSuite:
  test("should remove non-printable characters from String"):
    testWith(
      "Okay, here’s a breakdown of the key information from the document you provided, categorized for clarity:",
      "okay, heres a breakdown of the key information from the document you provided, categorized for clarity:",
    )

  test("should remove non-printable characters from String"):
    testWith(
      "Okay, here's a breakdown of the provided text, categorized for clarity:",
      "okay, here's a breakdown of the provided text, categorized for clarity:",
    )

  private def testWith(text: String, expected: String): Unit =
    val obtained = CanonicalString.from(text)
    assertEquals(obtained, expected)
