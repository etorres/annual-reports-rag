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
        val lifted = Arrow[Function1].split(
          (columns: List[Column]) => Some(columns),
          (rows: List[List[JsonData]]) => rows.headOption,
        )
        IO.fromOption(lifted(items).tupled)(IllegalArgumentException("Expected exactly one result"))
