package es.eriktorr
package report

import embedding.db.{ElasticClient, VectorStoreRouter}
import ollama.api.{HttpClient, OllamaApiClient, OllamaModel}
import report.api.Ranking
import report.application.QuestionAnsweringConfig

import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.DurationInt

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val question = "Who is the Board Chairman in the company \"Canadian Banc Corp.\"?"
    val config = QuestionAnsweringConfig.localContainerFor(OllamaModel.Gemma3)
    val verbose = false
    (for
      logger <- Resource.eval(Slf4jLogger.create[IO])
      given Logger[IO] = logger
      httpClient <- HttpClient.resource(30.seconds)
      ollamaApiClient = OllamaApiClient(httpClient, config.ollamaConfig)
      elasticClient <- ElasticClient.resource(config.elasticConfig)
      vectorStoreRouter = VectorStoreRouter.impl(elasticClient)
      ranking = Ranking.impl(config.ollamaConfig, verbose)
    yield (logger, ollamaApiClient, ranking, vectorStoreRouter)).use:
      case (logger, ollamaApiClient, ranking, vectorStoreRouter) =>
        for
          _ <- ollamaApiClient.preload()
          _ <- logger.info(s"You asked: $question")
          vectorResults <- vectorStoreRouter.bestMatchFor(question, 30)
          top10VectorResults = vectorResults.sortBy(_.score).reverse.take(30)

          _ =
            // TODO
            top10VectorResults.foreach(x => println(s" >> TOP_10: ${x.index}, page: ${x.page}"))
          _ <- if true then IO.raiseError(RuntimeException()) else IO.unit
          // TODO

          _ <- ranking.rank(question, top10VectorResults)
          _ <- logger.info("Here is my response:")
        yield ExitCode.Success
