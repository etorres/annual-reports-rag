package es.eriktorr
package application

import com.comcast.ip4s.{Host, Port, host, port}
import org.apache.http.HttpHost

final case class ElasticConfig(host: Host, insecure: Boolean, namespace: String, port: Port):
  def httpHost: HttpHost =
    val protocol = if insecure then "http" else "https"
    HttpHost.create(s"$protocol://$host:$port")
  def indexNameFrom(index: String) = s"${namespace}_$index"

object ElasticConfig:
  def localContainerFor(namespace: String): ElasticConfig =
    ElasticConfig(host = host"localhost", insecure = true, namespace = namespace, port = port"9200")
