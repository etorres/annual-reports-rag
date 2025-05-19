package es.eriktorr
package embedding.db

import cats.data.NonEmptyList

final case class ChunkSet(
    filename: String,
    index: String,
    chunks: NonEmptyList[ChunkSet.Chunk],
)

object ChunkSet:
  final case class Chunk(
      page: Int,
      score: Double,
      text: String,
  )
