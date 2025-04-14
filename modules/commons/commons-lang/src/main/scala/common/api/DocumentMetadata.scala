package es.eriktorr
package common.api

enum DocumentMetadata(val name: String):
  case Filename extends DocumentMetadata("Filename")
  case PageNumber extends DocumentMetadata("PageNumber")
  case Sha1FileChecksum extends DocumentMetadata("Sha1FileChecksum")
  case Title extends DocumentMetadata("Title")

object DocumentMetadata:
  val editionFields: List[DocumentMetadata] = List(
    DocumentMetadata.Filename,
    DocumentMetadata.Sha1FileChecksum,
    DocumentMetadata.Title,
  )
