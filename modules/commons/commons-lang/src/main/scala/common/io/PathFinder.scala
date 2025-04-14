package es.eriktorr
package common.io

import cats.effect.IO

import java.util.Locale

object PathFinder:
  def find(dir: os.Path, fileType: FileType): IO[List[os.Path]] =
    if os.isDir(dir, followLinks = false) then
      IO.blocking:
        os.walk(path = dir, skip = _.ext.toLowerCase(Locale.US) != fileType.ext, maxDepth = 1)
          .toList
    else IO.pure(List.empty)
