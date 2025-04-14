package es.eriktorr
package common.io

import cats.effect.{IO, Resource, ResourceIO}

object TempDir:
  def make(prefix: String): ResourceIO[os.Path] =
    Resource.make {
      IO.blocking(os.temp.dir(prefix = prefix))
    } { tempDir =>
      IO.blocking(os.remove.all(tempDir))
    }
