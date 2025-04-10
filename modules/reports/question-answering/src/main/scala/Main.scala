package es.eriktorr

import AnyRefExtensions.ignoreResult

import api.OllamaChatModelBuilderExtensions.responseFormat
import api.{DocumentMetadata, OllamaModel}
import application.QuestionAnsweringConfig
import db.VectorStoreRouter

import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.service.{AiServices, MemoryId, SystemMessage, UserMessage}

import java.time.Duration as JDuration
import java.util.concurrent.{ExecutorService, ForkJoinPool}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.chaining.*

object Main:
  @main
  def run(): Unit =
    val question = "Who is the Board Chairman in the company \"Canadian Banc Corp.\"?"

    val config = QuestionAnsweringConfig.singleProcessor(OllamaModel.Gemma3)
    val verbose = false

    val vectorStoreRouter = VectorStoreRouter.impl(
      config.elasticConfig,
      "summary",
      DocumentMetadata.IndexName,
    )
    vectorStoreRouter.refreshIndex()
    val vectorResult = vectorStoreRouter.bestMatchFor(question, 3)

    val chatModel = OllamaChatModel
      .builder()
      .baseUrl(config.ollamaConfig.baseUrl)
      .logRequests(verbose)
      .logResponses(verbose)
      .maxRetries(3)
      .modelName(config.ollamaConfig.model.name)
      .repeatPenalty(0.8d)
      .responseFormat(config.ollamaConfig.model)
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

    val forkJoinPool: ExecutorService = ForkJoinPool(4)
    given forkJoinExecutionContext: ExecutionContext =
      ExecutionContext.fromExecutorService(forkJoinPool)
    val promise = Future.traverse(vectorResult.zipWithIndex) { case (vectorResult, idx) =>
      vectorResult.pipe: vectorResult =>
        assistant
          .chat(
            idx,
            s"""Here is the retrieved text block:
               |${vectorResult.text}""".stripMargin,
          )
          .ignoreResult()
        val response = assistant.chat(
          idx,
          s"""Here is the question:
             |$question""".stripMargin,
        )
        Future.successful(vectorResult -> response)
    }
    val responses = Await.result(promise, 5.minutes)

    responses.foreach: (vectorResult, response) =>
      println(
        s" >> ${vectorResult.indexName} with score ${vectorResult.score} and response: $response",
      )

    sys.exit(0)

  private trait Assistant:
    @SystemMessage(fromResource = "prompt_templates/rank.txt")
    def chat(@MemoryId sessionId: Int, @UserMessage message: String): String
