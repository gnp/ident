import sbt._

object Dependencies {
  val CirceVersion = "0.14.5"
  lazy val CirceCore = "io.circe" %% "circe-core" % CirceVersion
  lazy val CirceGeneric = "io.circe" %% "circe-generic" % CirceVersion
  lazy val CirceParser = "io.circe" %% "circe-parser" % CirceVersion

  val LogbackVersion = "1.4.8"
  lazy val Logback = "ch.qos.logback" % "logback-classic" % LogbackVersion

  val Slf4JVersion = "2.0.7"
  lazy val Slf4JApi = "org.slf4j" % "slf4j-api" % Slf4JVersion
  lazy val JclOverSlf4J = "org.slf4j" % "jcl-over-slf4j" % Slf4JVersion
  lazy val Log4JOverSlf4J = "org.slf4j" % "log4j-over-slf4j" % Slf4JVersion
  lazy val JulToSlf4J = "org.slf4j" % "jul-to-slf4j" % Slf4JVersion

  val ZioJsonVersion = "0.5.0"
  lazy val ZioJson = "dev.zio" %% "zio-json" % ZioJsonVersion

  val ZioSchemaVersion = "0.4.12"
  lazy val ZioSchema = "dev.zio" %% "zio-schema" % ZioSchemaVersion
  lazy val ZioSchemaDerivation = "dev.zio" %% "zio-schema-derivation" % ZioSchemaVersion
  lazy val ZioSchemaJson = "dev.zio" %% "zio-schema-json" % ZioSchemaVersion

  val ZioVersion = "2.0.15"
  lazy val Zio = "dev.zio" %% "zio" % ZioVersion
  lazy val ZioTest = "dev.zio" %% "zio-test" % ZioVersion
  lazy val ZioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % ZioVersion
  lazy val ZioTestSbt = "dev.zio" %% "zio-test-sbt" % ZioVersion
}
