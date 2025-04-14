package es.eriktorr
package common.security

import cats.effect.IO
import scodec.bits.BitVector

import java.security.MessageDigest as JMessageDigest

object MessageDigest:
  def checksum(path: os.Path): IO[String] =
    IO.delay:
      val hash = JMessageDigest.getInstance("SHA-1")
      os.read
        .chunks(path, chunkSize = 8192)
        .foreach:
          case (bytes, bytesRead) =>
            hash.update(bytes, 0, bytesRead)
      BitVector(hash.digest).toHex
