package es.eriktorr
package common.spec

import munit.Tag

object TestFilters:
  val online: Tag = new Tag("online")

  val envVarsName: String = "SBT_TEST_ENV_VARS"
