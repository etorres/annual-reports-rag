package es.eriktorr
package domain

import api.{OllamaApiClient, ReportLoader}
import application.{ElasticConfig, OllamaConfig, ReportLoaderConfig}
import db.{VectorStore, VectorStoreBuilder}

import scala.util.chaining.given

final class ReportLoaderService(config: ReportLoaderConfig):
  def loadReports(reportsPath: os.Path): Unit =
    val ollamaConfig = preloadModel(config.ollamaConfig)
    val (summaryVectorStore, vectorStoreBuilder) = initVectorDatabases(config.elasticConfig)
    ReportLoader(ollamaConfig, summaryVectorStore, vectorStoreBuilder).allReportsFrom(
      reportsPath,
    )

  private def preloadModel(ollamaConfig: OllamaConfig) =
    ollamaConfig.tap(OllamaApiClient.preload)

  private def initVectorDatabases(elasticConfig: ElasticConfig) =
    val summaryVectorStore = VectorStore.impl(elasticConfig, "summary")
    val vectorStoreBuilder = VectorStoreBuilder(elasticConfig)
    summaryVectorStore -> vectorStoreBuilder
