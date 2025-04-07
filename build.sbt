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

lazy val usingLog4j: Project => Project = withBaseSettings.compose(
  _.settings(
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "org.apache.logging.log4j" % "log4j-core" % "2.24.3" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3" % Runtime,
    ),
  ),
)

lazy val `commons-embedding` = project
  .in(file("modules/commons/commons-embedding"))
  .configure(usingLog4j)
  .settings(
    libraryDependencies ++= Seq(
      "com.comcast" %% "ip4s-core" % "3.6.0",
      "dev.langchain4j" % "langchain4j" % "1.0.0-beta2",
      "dev.langchain4j" % "langchain4j-elasticsearch" % "1.0.0-beta2",
      "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2-q" % "1.0.0-beta2",
    ),
  )
  .dependsOn(
    `commons-lang` % "test->test;compile->compile",
  )

lazy val `commons-lang` = project
  .in(file("modules/commons/commons-lang"))
  .configure(usingLog4j)
  .settings(
    libraryDependencies ++= Seq(
      "com.comcast" %% "ip4s-core" % "3.6.0",
      "com.lihaoyi" %% "geny" % "1.1.1",
      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "ujson" % "4.1.0",
    ),
  )

lazy val `report-indexing` = project
  .in(file("modules/reports/report-indexing"))
  .configure(usingLog4j)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.11.4",
      "dev.langchain4j" % "langchain4j" % "1.0.0-beta2",
      "dev.langchain4j" % "langchain4j-document-parser-apache-pdfbox" % "1.0.0-beta2",
      "dev.langchain4j" % "langchain4j-ollama" % "1.0.0-beta2",
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
    ),
  )
  .dependsOn(
    `commons-embedding` % "test->test;compile->compile",
    `sample-reports` % "test->test;compile->compile",
  )

lazy val `sample-reports` = project
  .in(file("modules/reports/sample-reports"))
  .configure(withBaseSettings)
  .settings(
    libraryDependencies ++= Seq(),
  )

lazy val root = (project in file("."))
  .aggregate(
    `commons-embedding`,
    `commons-lang`,
    `report-indexing`,
    `sample-reports`,
  )
  .settings(
    name := "annual-reports-rag",
    Compile / doc / sources := Seq(),
    publish := {},
    publishLocal := {},
  )
