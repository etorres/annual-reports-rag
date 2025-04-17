package es.eriktorr
package embedding.db

import common.data.error.HandledError

sealed abstract class ElasticError(message: String) extends HandledError(message)

object ElasticError:
  final case class ListIndexesFailed(failedShards: Double)
      extends ElasticError(s"Failed to list indexes with $failedShards failed shards")
  final case class RefreshFailed(indexName: String, failedShards: Double)
      extends ElasticError(
        s"The index $indexName cannot be refreshed with $failedShards failed shards",
      )
  final case class QueryFailed(sql: String) extends ElasticError(s"Failed to query: $sql")
