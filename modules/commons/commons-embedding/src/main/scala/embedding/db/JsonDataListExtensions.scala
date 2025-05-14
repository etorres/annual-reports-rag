package es.eriktorr
package embedding.db

import co.elastic.clients.elasticsearch.sql.Column
import co.elastic.clients.json.JsonData
import jakarta.json.JsonString

object JsonDataListExtensions:
  extension (self: List[JsonData])
    def integerFrom(columns: List[Column], columnName: String): Either[Throwable, Int] =
      self
        .textFrom(columns, columnName)
        .flatMap: text =>
          text.toIntOption match
            case Some(value) => Right(value)
            case None =>
              Left(IllegalArgumentException(s"The value in column $columnName is not integer"))

    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    def textFrom(columns: List[Column], columnName: String): Either[Throwable, String] =
      columns.zipWithIndex
        .find:
          case (column, idx) =>
            column.name() == columnName
        .flatMap:
          case (_, idx) =>
            self.lift(idx).map(_.toJson.asInstanceOf[JsonString].getString)
      match
        case Some(value) => Right(value)
        case None => Left(IllegalArgumentException(s"Failed to get text column: $columnName"))
