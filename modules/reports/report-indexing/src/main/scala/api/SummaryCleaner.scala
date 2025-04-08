package es.eriktorr
package api

sealed trait SummaryCleaner:
  def clean(summary: String): String

object SummaryCleaner:
  object Noop extends SummaryCleaner:
    override def clean(summary: String): String = summary

  final class NotesRemover(delimiter: String) extends SummaryCleaner:
    override def clean(summary: String): String =
      summary.linesIterator.takeWhile(!_.contains(delimiter)).mkString("\n")

  def impl(ollamaModel: OllamaModel): SummaryCleaner =
    ollamaModel match
      case OllamaModel.Gemma3 => NotesRemover(delimiter = "---")
      case _ => Noop
