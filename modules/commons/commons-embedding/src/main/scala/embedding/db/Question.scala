package es.eriktorr
package embedding.db

import cats.Show

final case class Question(companyName: String, question: String)

object Question:
  given Show[Question] =
    Show.show: x =>
      s"Question: company=${x.companyName}, text=${x.question}"
