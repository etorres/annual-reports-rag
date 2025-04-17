package es.eriktorr
package report.api

import embedding.db.VectorResult
import ollama.api.OllamaChatModelBuilderExtensions.responseFormat
import ollama.application.OllamaConfig

import cats.effect.IO
import cats.implicits.toTraverseOps
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.service.{AiServices, MemoryId, SystemMessage, UserMessage}
import org.typelevel.log4cats.Logger

import java.time.Duration as JDuration

trait Ranking:
  def rank(question: String, vectorResults: List[VectorResult]): IO[Unit]

object Ranking:
  def impl(config: OllamaConfig, verbose: Boolean)(using logger: Logger[IO]): Ranking =
    val chatModel = OllamaChatModel
      .builder()
      .baseUrl(config.baseUrl)
      .logRequests(verbose)
      .logResponses(verbose)
      .maxRetries(3)
      .modelName(config.model.name)
      .repeatPenalty(0.8d)
      .responseFormat(config.model)
      .temperature(0.2d)
      .timeout(JDuration.ofMinutes(10L))
      .topK(40)
      .topP(0.9d)
      .build()
    val assistant = AiServices
      .builder(classOf[Assistant])
      .chatLanguageModel(chatModel)
      .chatMemoryProvider((sessionId: Any) =>
        MessageWindowChatMemory.builder().id(sessionId).maxMessages(10).build(),
      )
      .build()
    def chat(sessionId: Int, message: String) =
      IO.blocking:
        assistant.chat(sessionId, message)
    (question: String, vectorResults: List[VectorResult]) =>
      (vectorResults.zipWithIndex.traverse:
        case (vectorResult, idx) =>
          for
            _ <- logger.info(
              s"Ranking vector from index ${vectorResult.index}, it would take a few seconds...",
            )
            _ <- chat(
              idx,
              s"""Here is the retrieved text block:
                 |
                 |${vectorResult.text}""".stripMargin,
            )
            response <- chat(
              idx,
              s"""Here is the question:
                 |$question""".stripMargin,
            )
            relevanceScore <- RelevanceScore.from(response)
            _ <- IO.println(
              s"${vectorResult.index} with score ${vectorResult.score}, ranking $relevanceScore, and text:\n\n$response",
            ) // TODO
          yield ()
      ) *> IO.unit

  private trait Assistant:
    @SystemMessage(fromResource = "prompt_templates/rank.txt")
    def chat(@MemoryId sessionId: Int, @UserMessage message: String): String
