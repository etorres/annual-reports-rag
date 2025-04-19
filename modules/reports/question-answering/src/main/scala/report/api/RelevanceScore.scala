package es.eriktorr
package report.api

import common.data.error.HandledError
import report.api.RelevanceScore.RelevanceScoreError.UnsupportedFormatError

import cats.effect.IO
import org.typelevel.log4cats.Logger

import scala.util.matching.Regex

object RelevanceScore:
  def from(text: String)(using logger: Logger[IO]): IO[Double] =
    IO.fromOption[Double] {
      val fragment = text.linesIterator
        .filter(_.trim.nonEmpty)
        .dropWhile(x => !x.contains("Relevance Score"))
        .toList
      val cleanFragment = (fragment.take(2.min(fragment.length)) match
        case Nil => List.empty
        case ::(head, next) =>
          val cleanHead = head
            .substring(head.indexOf("Relevance Score"))
            .replaceAll("\\*", "")
            .replaceAll(" \\(0 to 1\\)", "")
          cleanHead :: next
      ).mkString(" ")
      cleanFragment match
        case relevanceScorePattern(score) => score.toDoubleOption
        case _ => Option.empty[Double]
    }(UnsupportedFormatError(s"Cannot extract the relevance score from: $text"))
      .recoverWith:
        case error: UnsupportedFormatError => logger.warn(error)("Failed with error") *> IO.pure(0d)

  private lazy val relevanceScorePattern: Regex =
    """^Relevance Score:\s(?<score>\d+\.?\d?)\s?""".r.unanchored

  sealed abstract class RelevanceScoreError(message: String) extends HandledError(message)

  object RelevanceScoreError:
    final case class UnsupportedFormatError(message: String) extends RelevanceScoreError(message)
