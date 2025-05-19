package es.eriktorr
package report.domain

import embedding.db.{ChunkSet, Question, VectorStoreRouter}
import report.api.{PageRebuilding, RankedPage, Ranking}
import report.application.QuestionAnsweringConfig

import cats.effect.IO
import cats.implicits.showInterpolator

final class ContentRetrieverService(
    config: QuestionAnsweringConfig,
    ranking: Ranking,
    vectorStoreRouter: VectorStoreRouter,
):
  def relevantContextFor(companyName: String, question: String): IO[String] =
    for
      topNRelevantChunks <- topNRelevantChunks(
        config.topN,
        companyName,
        question,
        vectorStoreRouter,
      )
      pages <- pagesFrom(topNRelevantChunks, vectorStoreRouter)
      rankedPages <- ranking.rank(topNRelevantChunks, pages, question)
      _ = // TODO
        topNRelevantChunks.chunks.toList.foreach(x =>
          println(s" >> TOP_N: ${topNRelevantChunks.index}, page: ${x.page}"),
        )
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
              s"\n**Page: ${page.page.page}**\n\n${page.page.text}"
            .mkString("\n")
          s"**Filename: $filename**\n$mergedPages"
      .toList
      .mkString("\n")

  private def pagesFrom(chunkSet: ChunkSet, vectorStoreRouter: VectorStoreRouter) =
    for
      documentResults <- vectorStoreRouter.findBy(
        chunkSet.index,
        chunkSet.chunks.map(_.page).distinct,
      )
      pages = PageRebuilding.rebuild(documentResults)
    yield pages

  private def topNRelevantChunks(
      n: Int,
      companyName: String,
      question: String,
      vectorStoreRouter: VectorStoreRouter,
  ) =
    for
      wrappedQuestion = Question(companyName, question)
      topNRelevantChunks <- vectorStoreRouter
        .topNRelevantChunks(n, wrappedQuestion)
        .getOrRaise(
          IllegalArgumentException(show"No relevant chunks found for question: $wrappedQuestion"),
        )
    yield topNRelevantChunks
