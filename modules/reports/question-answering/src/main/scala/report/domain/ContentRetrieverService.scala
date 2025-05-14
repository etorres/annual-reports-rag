package es.eriktorr
package report.domain

import embedding.db.{Question, VectorResult, VectorStoreRouter}
import report.api.{PageRebuilding, RankedPage, Ranking}
import report.application.QuestionAnsweringConfig

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.{catsSyntaxParallelFlatTraverse1, showInterpolator}

final class ContentRetrieverService(
    config: QuestionAnsweringConfig,
    ranking: Ranking,
    vectorStoreRouter: VectorStoreRouter,
):
  def relevantContextFor(companyName: String, question: String): IO[String] =
    for
      topNVectorResults <- topNRelevantChunks(
        config.topN,
        companyName,
        question,
        vectorStoreRouter,
      )
      pages <- pagesFrom(topNVectorResults, vectorStoreRouter)
      rankedPages <- ranking.rank(pages, question, topNVectorResults)
      _ = // TODO
        topNVectorResults.foreach(x => println(s" >> TOP_N: ${x.index}, page: ${x.page}"))
        pages.foreach(x => println(s" >> PAGE: ${x.filename}, ${x.page}"))
        rankedPages.foreach(x => println(show" >> PAGE: $x"))
      relevantContext = merge(rankedPages, 10)
    yield relevantContext

  private def merge(rankedPages: List[RankedPage], maximum: Int) =
    val topNPages = rankedPages.sortBy(_.score).reverse.take(maximum)
    val topNPagesGroupedByFilename = topNPages.groupBy(_.page.filename)
    topNPagesGroupedByFilename
      .map:
        case (filename, pages) =>
          val mergedPages = pages
            .sortBy(_.page.page)
            .map: page =>
              s"**Page: ${page.page.page}**\n\n${page.page.text}"
            .mkString("\n")
          s"**Filename: $filename**\n\n$mergedPages"
      .toList
      .mkString("\n")

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

  private def topNRelevantChunks(
      n: Int,
      companyName: String,
      question: String,
      vectorStoreRouter: VectorStoreRouter,
  ) =
    for
      vectorResults <- vectorStoreRouter.bestMatchFor(Question(companyName, question), n)
      topNVectorResults = vectorResults.sortBy(_.score).reverse.take(n)
    yield topNVectorResults
