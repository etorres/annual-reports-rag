package es.eriktorr
package embedding.db

import embedding.db.ElasticError.RefreshFailed

import cats.effect.IO
import co.elastic.clients.elasticsearch.indices.RefreshRequest
import dev.langchain4j.data.segment.TextSegment

import scala.jdk.CollectionConverters.given

trait VectorStore extends ElasticVectorStore:
  def add(textSegments: List[TextSegment]): IO[List[String]]
  def refreshIndex(): IO[Double]

object VectorStore:
  def impl(elasticClient: ElasticClient, index: String): VectorStore =
    new VectorStore:
      private val indexName = elasticClient.indexNameFrom(index)

      override def add(textSegments: List[TextSegment]): IO[List[String]] =
        for
          embeddingStore <- embeddingStoreFrom(elasticClient, indexName)
          embedded = textSegments.asJava
          embeddings = embeddingModel.embedAll(embedded).content()
          ids <- IO.blocking(embeddingStore.addAll(embeddings, embedded))
        yield ids.asScala.toList

      override def refreshIndex(): IO[Double] =
        for
          response <- IO.blocking:
            elasticClient.elasticsearchClient
              .indices()
              .refresh(RefreshRequest.Builder().index(indexName).build())
          shards = response.shards()
          result <-
            if shards.failed().doubleValue() > 0d then
              IO.raiseError(RefreshFailed(indexName, shards.failed().doubleValue()))
            else IO.pure(shards.successful().doubleValue())
        yield result
  end impl
