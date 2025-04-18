package es.eriktorr
package report

import embedding.db.{ElasticClient, VectorResult, VectorStoreRouter}
import ollama.api.{HttpClient, OllamaApiClient, OllamaModel}
import report.api.{PageRebuilding, Ranking}
import report.application.QuestionAnsweringConfig

import cats.data.NonEmptyList
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.catsSyntaxParallelFlatTraverse1
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
          topNVectorResults <- topNRelevantChunks(config.topN, question, vectorStoreRouter)
          pages <- pagesFrom(topNVectorResults, vectorStoreRouter)

          _ =
            // TODO
            topNVectorResults.foreach(x => println(s" >> TOP_N: ${x.index}, page: ${x.page}"))
            pages.foreach(x => println(s" >> PAGE: ${x.filename}, ${x.page}, ${x.text}"))
          _ <- if true then IO.raiseError(RuntimeException()) else IO.unit
          // TODO

          _ <- ranking.rank(question, topNVectorResults)
          _ <- logger.info("Here is my response:")
        yield ExitCode.Success

  private def topNRelevantChunks(n: Int, question: String, vectorStoreRouter: VectorStoreRouter) =
    for
      vectorResults <- vectorStoreRouter.bestMatchFor(question, n)
      topNVectorResults = vectorResults.sortBy(_.score).reverse.take(n)
    yield topNVectorResults

  private def pagesFrom(vectorResults: List[VectorResult], vectorStoreRouter: VectorStoreRouter) =
    for
      documentResults <- vectorResults
        .groupBy(_.index)
        .view
        .mapValues(_.map(_.page).distinct)
        .toList
        .parFlatTraverse:
          case (index, pages) =>
            vectorStoreRouter.findBy(index, NonEmptyList.fromListUnsafe(pages))
      pages = PageRebuilding.rebuild(documentResults)
    yield pages
