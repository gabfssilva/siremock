# SireMock

SireMock is a Scala wrapper for WireMock, which is a HTTP simulator that runs on the JVM.
The main idea of SireMock is to provide a more friendly api for Scala.

If you want to start with SireMock, first you need to add the dependency:

```scala
//only for Scala 2.12

resolvers += "gabfssilva releases" at "http://dl.bintray.com/gabfssilva/maven"

libraryDependencies += "io.github.gabfssilva" %% "siremock" % "0.0.4" % "test"
```

## The obligatory hello world

```scala
class MockFeatures extends FeatureSpec with Matchers with BeforeAndAfter with SireMock  {
  override val sireMockConfig: SireMockConfig = SireMockConfig(port = 8181)

  before {
    startSireMock
    resetSireMock
  }

  after {
    stopSireMock
  }

  feature("GET") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      mockGet(
        path = "/hello",
        withResponseBody = Some(expectedResponseBody)
      )

      val response = Http("http://localhost:8181/hello")
        .method("get")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200

      verifyGet("/hello", count = 1.exactlyStrategy)
    }
  }
}
```



