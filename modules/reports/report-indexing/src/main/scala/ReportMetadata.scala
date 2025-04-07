package es.eriktorr

enum ReportMetadata(val name: String):
  case Filename extends ReportMetadata("file_name")
  case IndexName extends ReportMetadata("IndexName")
  case Summary extends ReportMetadata("Summary")
  case Title extends ReportMetadata("Title")
