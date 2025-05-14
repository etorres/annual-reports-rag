ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.6.4"

ThisBuild / semanticdbEnabled := true
ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

addCommandAlias(
  "check",
  "; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
)

lazy val MUnitFramework = new TestFramework("munit.Framework")
// lazy val warts = Warts.unsafe.filter(_ != Wart.DefaultArguments)
lazy val warts = Warts.unsafe.filter(x => !Set(Wart.Any, Wart.DefaultArguments).contains(x)) // TODO

lazy val withBaseSettings: Project => Project = _.settings(
  Compile / doc / sources := Seq(),
  tpolecatDevModeOptions ++= Set(
    org.typelevel.scalacoptions.ScalacOptions.other("-java-output-version", List("21"), _ => true),
    org.typelevel.scalacoptions.ScalacOptions.warnOption("safe-init"),
    org.typelevel.scalacoptions.ScalacOptions.privateOption("explicit-nulls"),
  ),
  Compile / compile / wartremoverErrors ++= warts,
  Test / compile / wartremoverErrors ++= warts,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "1.1.0" % Test,
    "org.scalameta" %% "munit-scalacheck" % "1.1.0" % Test,
  ),
  Test / envVars := Map(
    "SBT_TEST_ENV_VARS" -> "true",
  ),
  Test / testFrameworks += MUnitFramework,
  Test / testOptions += Tests.Argument(MUnitFramework, "--exclude-tags=online"),
)

lazy val usingCatsEffect: Project => Project = withBaseSettings.compose(
  _.settings(
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-core" % "2.24.3" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3" % Runtime,
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "org.typelevel" %% "munit-cats-effect" % "2.1.0" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test,
    ),
  ),
)

lazy val usingLog4cats: Project => Project = usingCatsEffect.compose(
  _.settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.24.3" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3" % Runtime,
    ),
  ),
)

lazy val `commons-embedding` = project
  .in(file("modules/commons/commons-embedding"))
  .configure(usingLog4cats)
  .settings(
    libraryDependencies ++= Seq(
      "com.comcast" %% "ip4s-core" % "3.7.0",
      "dev.langchain4j" % "langchain4j" % "1.0.0-beta3",
      "dev.langchain4j" % "langchain4j-elasticsearch" % "1.0.0-beta3",
      "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2-q" % "1.0.0-beta3",
    ),
  )
  .dependsOn(
    `commons-ollama` % "test->test;compile->compile",
  )

lazy val `commons-lang` = project
  .in(file("modules/commons/commons-lang"))
  .configure(usingCatsEffect)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.11.4",
      "org.scodec" %% "scodec-bits" % "1.2.1",
    ),
  )

lazy val `commons-ollama` = project
  .in(file("modules/commons/commons-ollama"))
  .configure(usingLog4cats)
  .settings(
    libraryDependencies ++= Seq(
      "com.comcast" %% "ip4s-core" % "3.7.0",
      "com.softwaremill.sttp.client4" %% "cats" % "4.0.5",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.5",
      "com.softwaremill.sttp.client4" %% "slf4j-backend" % "4.0.5",
      "dev.langchain4j" % "langchain4j" % "1.0.0-beta3",
      "dev.langchain4j" % "langchain4j-ollama" % "1.0.0-beta3",
    ),
  )
  .dependsOn(
    `commons-lang` % "test->test;compile->compile",
  )

lazy val `question-answering` = project
  .in(file("modules/reports/question-answering"))
  .configure(usingCatsEffect)
  .settings(
    libraryDependencies ++= Seq(
      "dev.langchain4j" % "langchain4j" % "1.0.0-beta3",
      "dev.langchain4j" % "langchain4j-ollama" % "1.0.0-beta3",
    ),
  )
  .dependsOn(
    `commons-embedding` % "test->test;compile->compile",
    `commons-ollama` % "test->test;compile->compile",
  )

lazy val `report-indexing` = project
  .in(file("modules/reports/report-indexing"))
  .configure(usingCatsEffect)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % "3.12.0",
      "com.lihaoyi" %% "os-lib" % "0.11.4",
      "dev.langchain4j" % "langchain4j" % "1.0.0-beta3",
      "dev.langchain4j" % "langchain4j-document-parser-apache-pdfbox" % "1.0.0-beta3",
      "dev.langchain4j" % "langchain4j-ollama" % "1.0.0-beta3",
      "org.gnieh" %% "fs2-data-json-circe" % "1.11.3",
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
    ),
  )
  .dependsOn(
    `commons-embedding` % "test->test;compile->compile",
    `commons-ollama` % "test->test;compile->compile",
    `sample-reports` % "test->test;compile->compile",
  )

lazy val `sample-reports` = project
  .in(file("modules/reports/sample-reports"))
  .configure(withBaseSettings)

lazy val root = (project in file("."))
  .aggregate(
    `commons-embedding`,
    `commons-lang`,
    `commons-ollama`,
    `question-answering`,
    `report-indexing`,
    `sample-reports`,
  )
  .settings(
    name := "annual-reports-rag",
    Compile / doc / sources := Seq(),
    publish := {},
    publishLocal := {},
  )
