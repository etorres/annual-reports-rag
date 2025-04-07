package es.eriktorr

import com.comcast.ip4s.{host, port, Host, Port}
import org.apache.http.HttpHost

final case class ElasticConfig(host: Host, insecure: Boolean, port: Port):
  def httpHost: HttpHost =
    val protocol = if insecure then "http" else "https"
    HttpHost.create(s"$protocol://$host:$port")

object ElasticConfig:
  val localContainer: ElasticConfig =
    ElasticConfig(host = host"localhost", insecure = true, port"9200")
