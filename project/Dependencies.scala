import sbt._

object Dependencies {
  val CirceVersion = "0.14.5"
  lazy val CirceCore = "io.circe" %% "circe-core" % CirceVersion

  val LogbackVersion = "1.4.8"
  lazy val Logback = "ch.qos.logback" % "logback-classic" % LogbackVersion

  val ScalaTestVersion = "3.2.16"
  lazy val ScalaTest = "org.scalatest" %% "scalatest" % ScalaTestVersion

  val Slf4JVersion = "2.0.7"
  lazy val Slf4JApi = "org.slf4j" % "slf4j-api" % Slf4JVersion
  lazy val JclOverSlf4J = "org.slf4j" % "jcl-over-slf4j" % Slf4JVersion
  lazy val Log4JOverSlf4J = "org.slf4j" % "log4j-over-slf4j" % Slf4JVersion
  lazy val JulToSlf4J = "org.slf4j" % "jul-to-slf4j" % Slf4JVersion
}
