val scala212 = "2.12.10"
val scala213 = "2.13.0"

lazy val supportedScalaVersions = List(scala212, scala213)

val wiremockVersion = "2.24.1"

lazy val root = (project in file("."))
  .settings(
    resolvers += Resolver.jcenterRepo,
    scalaVersion := scala213,
    crossScalaVersions := supportedScalaVersions,
    name := "siremock",
    organization := "io.github.gabfssilva",
    version := "1.0.2",
    libraryDependencies ++= Seq(
      "com.github.tomakehurst" % "wiremock" % wiremockVersion,

      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "org.pegdown" % "pegdown" % "1.6.0" % "test",
      "org.scalaj" %% "scalaj-http" % "2.4.2" % "test"
    ),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    bintrayRepository := "maven",
    bintrayOrganization := Some("gabfssilva")
  )