package es.eriktorr
package embedding.db

import cats.arrow.Arrow
import cats.effect.IO
import cats.implicits.catsSyntaxTuple2Semigroupal
import co.elastic.clients.elasticsearch.sql.Column
import co.elastic.clients.json.JsonData

object ElasticExtensions:
  extension (self: IO[(List[Column], List[List[JsonData]])])
    def unique: IO[(List[Column], List[JsonData])] =
      self.flatMap: items =>
        IO.fromOption(
          exactlyOneRow(items).tupled,
        )(IllegalArgumentException("Expected exactly one result"))

  private lazy val exactlyOneRow =
    Arrow[Function1].split(
      (columns: List[Column]) => Some(columns),
      (rows: List[List[JsonData]]) =>
        rows match
          case head :: Nil => Some(head)
          case _ => None,
    )
