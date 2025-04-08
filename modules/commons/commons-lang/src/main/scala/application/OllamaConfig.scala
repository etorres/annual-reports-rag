package es.eriktorr
package application

import api.OllamaModel

import com.comcast.ip4s.{host, port, Host, Port}

final case class OllamaConfig(
    host: Host,
    insecure: Boolean,
    model: OllamaModel,
    port: Port,
    processors: Int,
):
  def baseUrl: String =
    val protocol = if insecure then "http" else "https"
    s"$protocol://$host:$port"

object OllamaConfig:
  def localContainerFor(model: OllamaModel, processors: Int): OllamaConfig =
    OllamaConfig(
      host = host"localhost",
      insecure = true,
      model = model,
      port = port"11434",
      processors = processors,
    )
