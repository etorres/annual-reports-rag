package es.eriktorr
package api

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}
import java.util.Map as JavaMap
import scala.jdk.CollectionConverters.given

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

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def variablesFrom(first: (String, String), others: (String, String)*): JavaMap[String, AnyRef] =
    Map.from(first +: others).asJava.asInstanceOf[JavaMap[String, AnyRef]]
