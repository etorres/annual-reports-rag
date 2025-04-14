package es.eriktorr
package report.api

import common.api.DocumentMetadata
import report.api.TextSegmentExtensions.copy

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment

import scala.annotation.tailrec

object ChunkTransformer:
  def transform(textSegment: TextSegment, parent: Document): TextSegment =
    @tailrec
    def copy(accumulator: TextSegment, keys: List[DocumentMetadata]): TextSegment =
      keys match
        case Nil => accumulator
        case ::(head, next) => copy(accumulator.copy(head, parent), next)
    end copy
    copy(textSegment, DocumentMetadata.values.toList)
