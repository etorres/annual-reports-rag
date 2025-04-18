package es.eriktorr
package report.api

import embedding.db.DocumentResult

import scala.annotation.tailrec

object PageRebuilding:
  def rebuild(documentResults: List[DocumentResult]): List[Page] =
    documentResults
      .groupMap(x => x.filename -> x.page)(x => (x.chunk, x.text))
      .map:
        case ((filename, page), chunks) =>
          @tailrec
          def overlapping(text: String, lines: List[String]): Int =
            lines match
              case Nil => 0
              case _ =>
                val next = lines.dropRight(1)
                if text.endsWith(next.mkString("\n"))
                then next.length
                else overlapping(text, next)
          @tailrec
          def deduplicate(accumulated: List[String], chunks: List[String]): List[String] =
            chunks match
              case Nil => accumulated
              case ::(head, next) =>
                val lines = head.linesIterator.toList
                val overlap = overlapping(accumulated.mkString("\n"), lines)
                deduplicate(accumulated ++ lines.drop(overlap), next)
          val sortedChunks = chunks
            .sortBy:
              case (chunk, _) => chunk
            .map:
              case (_, text) => text
          val text = deduplicate(List.empty, sortedChunks).mkString("\n")
          Page(filename, page, text)
      .toList
