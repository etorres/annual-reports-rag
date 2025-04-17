package es.eriktorr
package report.api

import cats.effect.IO

import scala.util.matching.Regex

object RelevanceScore:
  def from(text: String): IO[Double] =
    IO.fromOption[Double](
      List(text.linesIterator.filter(_.trim.nonEmpty).mkString(" "))
        .filter(_.contains("Relevance Score"))
        .map:
          case relevanceScorePattern(_, _, score) => score.toDoubleOption
          case _ => Option.empty[Double]
        .collectFirst:
          case Some(value) => value,
    )(IllegalArgumentException(s"Cannot extract the relevance score from: $text"))

  private lazy val relevanceScorePattern: Regex =
    """\*\*(\d+\.\s)?(Relevance Score:[\\*]{0,2}\s)(?<score>\d+\.\d+)""".r.unanchored
