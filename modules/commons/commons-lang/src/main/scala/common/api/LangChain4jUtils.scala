package es.eriktorr
package common.api

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

object LangChain4jUtils:
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def pathTo(
      resource: String,
      classLoader: ClassLoader = Thread.currentThread().getContextClassLoader,
  ): Path =
    Option(classLoader.getResource(resource)) match
      case Some(url) => Paths.get(url.toURI)
      case None =>
        throw FileNotFoundException(
          s"Resource '$resource' was not found in the classpath from the given classloader",
        )
