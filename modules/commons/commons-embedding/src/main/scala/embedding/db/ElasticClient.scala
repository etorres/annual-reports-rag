package es.eriktorr
package embedding.db

import embedding.application.ElasticConfig

import cats.effect.{IO, Resource, ResourceIO}
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.elasticsearch.client.RestClient

final case class ElasticClient(
    elasticConfig: ElasticConfig,
    elasticsearchClient: ElasticsearchClient,
    restClient: RestClient,
):
  def indexNameFrom(index: String): String = elasticConfig.indexNameFrom(index)

object ElasticClient:
  def resource(elasticConfig: ElasticConfig): ResourceIO[ElasticClient] =
    for
      restClient <- Resource.fromAutoCloseable:
        IO.delay:
          RestClient.builder(elasticConfig.httpHost).build()
      transport <-
        Resource.fromAutoCloseable:
          IO.delay:
            RestClientTransport(restClient, JacksonJsonpMapper())
      elasticsearchClient = ElasticsearchClient(transport)
    yield ElasticClient(elasticConfig, elasticsearchClient, restClient)
