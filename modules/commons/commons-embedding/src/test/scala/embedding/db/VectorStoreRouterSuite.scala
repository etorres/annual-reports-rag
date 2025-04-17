package es.eriktorr
package embedding.db

import common.spec.TestFilters.online
import embedding.application.ElasticConfig
import embedding.db.VectorStoreRouterSuite.TestCase

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class VectorStoreRouterSuite extends CatsEffectSuite:
  test("should load pages from a given index (sample 1)".tag(online).ignore):
    testWith(VectorStoreRouterSuite.sample1)

  test("should load pages from a given index (sample 2)".tag(online).ignore):
    testWith(VectorStoreRouterSuite.sample2)

  test("should load pages from a given index (sample 3)".tag(online).ignore):
    testWith(VectorStoreRouterSuite.sample3)

  private def testWith(testCase: TestCase) =
    val elasticConfig = ElasticConfig.localContainerFor("embeddings")
    (for
      logger <- Resource.eval(Slf4jLogger.create[IO])
      given Logger[IO] = logger
      elasticClient <- ElasticClient.resource(elasticConfig)
      vectorStoreRouter = VectorStoreRouter.impl(elasticClient)
    yield vectorStoreRouter)
      .use: vectorStoreRouter =>
        for
          documentResults <- vectorStoreRouter.findBy(
            elasticConfig.indexNameFrom(testCase.index),
            testCase.pages,
          )
          obtained = documentResults.length
        yield obtained
      .assertEquals(testCase.numChunks.sum)

object VectorStoreRouterSuite:
  final private case class TestCase(index: String, pages: NonEmptyList[Int], numChunks: List[Int])

  private val sample1 = TestCase(
    index = "8f481abdbea65dc187463747496b16ced4256a3e",
    pages = NonEmptyList.of(1, 2, 3),
    numChunks = List(6, 11, 6),
  )

  private val sample2 = TestCase(
    index = "672f572896d6cbfa53db042603cb5fe36119d831",
    pages = NonEmptyList.one(1),
    numChunks = List(4),
  )

  private val sample3 = TestCase(
    index = "fc80d59877b4ae21911591b53664b2da1324cf25",
    pages = NonEmptyList.one(1),
    numChunks = List(5),
  )
