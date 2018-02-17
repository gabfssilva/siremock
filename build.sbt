name := "siremock"
organization := "io.github.gabfssilva"
version := "0.0.3"
scalaVersion := "2.12.4"

resolvers += Resolver.jcenterRepo

bintrayOrganization := Some("gabfssilva")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

val wiremockVersion = "2.11.0"

libraryDependencies ++= Seq(
  "com.github.tomakehurst" % "wiremock" % wiremockVersion,

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test",
  "org.scalaj" %% "scalaj-http" % "2.3.0" % "test"
)