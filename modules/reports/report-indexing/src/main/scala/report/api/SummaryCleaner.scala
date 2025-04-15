package es.eriktorr
package report.api

import ollama.api.OllamaModel

import es.eriktorr.common.CanonicalString

import scala.annotation.tailrec
import scala.util.chaining.*

sealed trait SummaryCleaner:
  def clean(summary: String): String

object SummaryCleaner:
  object Noop extends SummaryCleaner:
    override def clean(summary: String): String = summary

  case object GarbageRemover extends SummaryCleaner:
    override def clean(summary: String): String =
      cleanHeader(
        summary.linesIterator.toList,
        List("okay, here's a breakdown of ", "okay, heres a breakdown of "),
      ).pipe: lines =>
        cleanFooter(lines, List("---", "Do you want me to")).pipe: lines =>
          lines.mkString("\n")
    private def cleanHeader(lines: List[String], headers: List[String]) =
      @tailrec
      def clean(accumulator: List[String]): List[String] =
        accumulator match
          case Nil => accumulator
          case ::(head, next)
              if head.trim.nonEmpty && headers
                .forall(header => !CanonicalString.from(head).startsWith(header)) =>
            accumulator
          case ::(head, next) => clean(next)
      clean(lines)
    private def cleanFooter(lines: List[String], stopTags: List[String]) =
      lines.takeWhile: line =>
        stopTags.forall: stopTag =>
          !line.contains(stopTag)

  def impl(ollamaModel: OllamaModel): SummaryCleaner =
    ollamaModel match
      case OllamaModel.Gemma3 => GarbageRemover
      case _ => Noop
