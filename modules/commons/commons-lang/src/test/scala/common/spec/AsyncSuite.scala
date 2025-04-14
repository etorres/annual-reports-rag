package es.eriktorr
package common.spec

import munit.CatsEffectSuite

abstract class AsyncSuite extends CatsEffectSuite:
  def verbose: Boolean = false
