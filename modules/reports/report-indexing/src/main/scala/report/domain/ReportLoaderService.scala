package es.eriktorr
package report.domain

import report.api.ReportLoader

import cats.effect.IO

final class ReportLoaderService(reportLoader: ReportLoader):
  def loadReportsFrom(path: os.Path): IO[Int] =
    reportLoader.loadReportsFrom(path)
