package es.eriktorr
package embedding.db

final case class VectorResult(
    filename: String,
    index: String,
    page: Int,
    score: Double,
    text: String,
)
