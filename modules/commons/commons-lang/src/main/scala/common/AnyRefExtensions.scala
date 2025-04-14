package es.eriktorr
package common

object AnyRefExtensions:
  extension [T](self: T) def ignoreResult(): Unit = ()
