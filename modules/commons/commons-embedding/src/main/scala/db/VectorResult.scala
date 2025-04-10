package es.eriktorr
package db

final case class VectorResult(id: String, indexName: String, score: Double, text: String)
