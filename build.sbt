name := "sire-mock"
organization := "io.gabfssilva"
version := "0.0.1"
scalaVersion := "2.12.4"

resolvers += Resolver.jcenterRepo

val wiremockVersion = "2.11.0"

libraryDependencies ++= Seq(
  "com.github.tomakehurst" % "wiremock" % wiremockVersion,

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test"
)
