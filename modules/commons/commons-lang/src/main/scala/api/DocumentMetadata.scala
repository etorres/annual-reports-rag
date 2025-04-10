package es.eriktorr
package api

enum DocumentMetadata(val name: String):
  case Filename extends DocumentMetadata("Filename")
  case IndexName extends DocumentMetadata("IndexName")
  case Summary extends DocumentMetadata("Summary")
  case Title extends DocumentMetadata("Title")
