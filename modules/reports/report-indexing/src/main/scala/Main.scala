package es.eriktorr

import com.typesafe.scalalogging.StrictLogging

// TODO: Gemma3, Mistral, Phi4
object Main extends StrictLogging:
  @main
  def run(): Unit =
    // preload model
    val ollamaConfig = OllamaConfig.localContainerFor(OllamaModel.Gemma3, 1)
    OllamaApiClient.preload(ollamaConfig)
    // vector database
    val elasticConfig = ElasticConfig.localContainer
    val summaryVectorStore = VectorStore.impl(elasticConfig, "summary")
    val vectorStoreBuilder = VectorStoreBuilder(elasticConfig, "embeddings")
    // load reports
    val reportPath = os.pwd / "modules" / "reports"
    val samplesPath = reportPath / "sample-reports" / "src" / "main" / "resources" / "one"
    ReportLoader(ollamaConfig, summaryVectorStore, vectorStoreBuilder).allReportsFrom(
      samplesPath,
    )
    // check
    val vectorStoreRouter = VectorStoreRouter.impl(
      elasticConfig,
      "summary",
      ReportMetadata.Summary.name,
    )
    vectorStoreRouter.refreshIndex()
    val response = vectorStoreRouter.indexNameFor(
      "Who is the Board Chairman in the company \"Canadian Banc Corp.\"?",
    )
    println(s" >> RESPONSE: $response") // TODO
    sys.exit(0)
