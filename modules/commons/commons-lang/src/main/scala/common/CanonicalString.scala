package es.eriktorr
package common

import java.text.Normalizer
import java.util.Locale

object CanonicalString:
  def from(text: String): String = Normalizer
    .normalize(text, Normalizer.Form.NFKD)
    .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
    .replaceAll("[^\\x00-\\x7F]", "")
    .replaceAll("\\p{Cntrl}&&[^\r\n\t]", "")
    .replaceAll("\\p{C}", "")
    .trim()
    .toLowerCase(Locale.US)
