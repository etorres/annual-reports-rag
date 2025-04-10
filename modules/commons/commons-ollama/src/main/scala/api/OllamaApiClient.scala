package es.eriktorr
package api

import application.OllamaConfig

import com.typesafe.scalalogging.StrictLogging
import requests.Session

import scala.concurrent.duration.DurationInt
import scala.util.chaining.*

object OllamaApiClient extends StrictLogging:
  def preload(config: OllamaConfig): Unit =
    requests
      .Session(readTimeout = 30.minutes.toMillis.toInt)
      .pipe:
        pull(config)
      .pipe:
        generate(config)

  private def generate(config: OllamaConfig)(session: Session): Unit =
    logger.info(s"Generating: ${config.model.name}, it would take a few seconds...")
    val response = session
      .post(
        s"${config.baseUrl}/api/generate",
        data = ujson.Obj("keep_alive" -> "30m", "model" -> config.model.name, "stream" -> false),
      )
    val statusCode = response.statusCode
    assert(
      statusCode == 200,
      s"Server status code is $statusCode when trying to generate the model",
    )
    val json = ujson.read(response.text())
    val done = if json("done").bool then "done" else "pending"
    val doneReason = json("done_reason").strOpt.getOrElse("none")
    assert(
      done == "done" && doneReason == "load",
      s"The task is $done and the reason is $doneReason when trying to generate the model",
    )

  private def pull(config: OllamaConfig)(session: Session): Session =
    logger.info(
      s"Pulling ${config.model.name} from ${config.baseUrl}, it would take several minutes...",
    )
    val response = session
      .post(
        s"${config.baseUrl}/api/pull",
        data = ujson.Obj("insecure" -> true, "model" -> config.model.name, "stream" -> false),
      )
    val statusCode = response.statusCode
    assert(
      statusCode == 200,
      s"Server status code is $statusCode when trying to pull the model",
    )
    val json = ujson.read(response.text())
    val status = json("status").str
    assert(
      status == "success",
      s"The response status field is $status when trying to pull the model",
    )
    session
