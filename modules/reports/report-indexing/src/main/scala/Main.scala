package es.eriktorr

// TODO: Gemma3, Mistral, Phi4
object Main:
  @main
  def run(): Unit =
    // preload model
    val ollamaConfig = OllamaConfig.localContainerFor(OllamaModel.Gemma3, 1)
    OllamaApiClient.preload(ollamaConfig)
    // init vector databases
    val elasticConfig = ElasticConfig.localContainerFor("embeddings")
    val summaryVectorStore = VectorStore.impl(elasticConfig, "summary")
    val vectorStoreBuilder = VectorStoreBuilder(elasticConfig)
    // load reports from path
    val reportPath = os.pwd / "modules" / "reports"
    val samplesPath = reportPath / "sample-reports" / "src" / "main" / "resources" / "simple"
    ReportLoader(ollamaConfig, summaryVectorStore, vectorStoreBuilder).allReportsFrom(
      samplesPath,
    )
    // check
    val vectorStoreRouter = VectorStoreRouter.impl(
      elasticConfig,
      "summary",
      ReportMetadata.IndexName.name,
    )
    vectorStoreRouter.refreshIndex()
    val response = vectorStoreRouter.indexNameFor(
      "Who is the Board Chairman in the company \"Canadian Banc Corp.\"?",
    )
    println(s" >> RESPONSE: $response") // TODO
    sys.exit(0)
