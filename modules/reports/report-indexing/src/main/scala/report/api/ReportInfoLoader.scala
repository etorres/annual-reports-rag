package es.eriktorr
package report.api

import cats.data.OptionT
import cats.effect.IO
import fs2.data.json
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.jq.Compiler
import fs2.data.json.jq.literals.*
import fs2.io.file.{Files, Path}

final class ReportInfoLoader(path: os.Path):
  def findReportInfoBy(
      sha1: String,
      simple: Boolean = false,
  ): OptionT[IO, ReportInfo] =
    OptionT:
      for
        transformationPipe <- Compiler[IO].compile(
          if simple then ReportInfoLoader.mandatoryFields
          else ReportInfoLoader.allFields,
        )
        maybeReportInfo <- Files[IO]
          .readUtf8(Path(path.toString))
          .through(json.tokens)
          .through(transformationPipe)
          .through(codec.deserialize[IO, ReportInfo])
          .find(_.sha1 == sha1)
          .compile
          .last
      yield maybeReportInfo

object ReportInfoLoader:
  private val allFields = jq""".[]"""
  private val mandatoryFields =
    jq""".[] | {
          "sha1": .sha1,
          "company_name": .company_name,
          "cur": .cur,
          "major_industry": .major_industry
        }"""
