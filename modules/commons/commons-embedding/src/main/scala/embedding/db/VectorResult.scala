package es.eriktorr
package embedding.db

final case class VectorResult(
    id: String,
    index: String,
    page: Int,
    score: Double,
    text: String,
)
