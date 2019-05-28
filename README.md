# SireMock

SireMock is a Scala wrapper for WireMock, which is a HTTP simulator that runs on the JVM.
The main idea of SireMock is to provide a more friendly api for Scala.

If you want to start with SireMock, first you need to add the dependency:

```scala
//only for Scala 2.12

resolvers += "gabfssilva releases" at "http://dl.bintray.com/gabfssilva/maven"

libraryDependencies += "io.github.gabfssilva" %% "siremock" % "0.0.5" % "test"
```

## The obligatory hello world

```scala
class MockFeatures
  extends FeatureSpec
    with Matchers
    with SireMockServer
    with BeforeAndAfter {

  private val port = 8183

  override val wireMockServer: WireMockServer = new WireMockServer(port)

  before {
    startWireMock()
  }

  after {
    resetWireMock()
    stopWireMock()
  }

  feature("GET") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      (on(urlEqualTo("/hello")) get) returning (aResponse withBody expectedResponseBody)

      val resp = Http("http://localhost:" + port + "/hello").method("get").asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
    }
 }
}
```

Of course, you can use it without postfix operator notation:

```scala
val expectedResponseBody = """{"hello":"world"}"""

on(urlEqualTo("/hello")
  .get
  .returning(
    aResponse.withBody(expectedResponseBody)
  )

val resp = Http("http://localhost:" + port + "/hello").method("get").asString

resp.body shouldBe expectedResponseBody
resp.code shouldBe 200
```



