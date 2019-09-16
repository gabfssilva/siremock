# SireMock

[![Download](https://api.bintray.com/packages/gabfssilva/maven/siremock/images/download.svg) ](https://bintray.com/gabfssilva/maven/siremock/_latestVersion)

SireMock is a Scala wrapper for WireMock, which is a HTTP simulator that runs on the JVM.
The main idea of SireMock is to provide a more friendly api for Scala.


## Setup

If you want to start with SireMock, first you need to add the dependency:

```scala
//works for both Scala 2.12 and 2.13

resolvers += "gabfssilva releases" at "http://dl.bintray.com/gabfssilva/maven"

libraryDependencies += "io.github.gabfssilva" %% "siremock" % "1.0.2" % "test"
```

Also, you need to instantiate WireMock to start with SireMock.

This is an example using Scalatest:


```scala
class MockFeatures extends FeatureSpec with Matchers with BeforeAndAfter with SireMockServer {
  private val port = 8183

  override val wireMockServer: WireMockServer = new WireMockServer(port)

  before {
    startWireMock()
  }

  after {
    resetWireMock()
    stopWireMock()
  }
  
  //tests here
}
```

If you want to run WireMock as client, you can also use the SireMockClient trait:

```scala
class MockFeatures extends FeatureSpec with Matchers with BeforeAndAfter with SireMockClient {
  private val port = 8183

  override val wireMockClient: WireMock = new WireMock("localhost", port)

  after {
    resetWireMock()
  }
  
  //tests here
}
```

## Stubbing

SireMock provides much better Scala-like DSL for you to work with WireMock.

The basic syntax is:

```scala
(request) respond (response)
```

You can construct your request and response mappings as builders:

```scala
(on(urlEqualTo("/hello")) get) respond (aResponse withBody "hello, world!")
```

This is a complex example to show what you can do with the DSL:

```scala
(((on(urlEqualTo("/hello")) post)
        withAuth BasicAuth("test", "test")
        withContentType "application/json"
        withBody equalToJson("""{ "hello": "world!" }""")
        withHeaders ("X-My-Header" -> equalTo("MyHeaderValue"), "X-My-Other-Header" -> equalTo("MyHeaderValue2")))
        respond (
          aResponse
            withContentType "application/json"
            withStatus 201
            withBody expectedResponseBody
            withHeaders("X-My-Header-Response" -> "MyHeaderValueResponse")
            withDelay(2 seconds)
          )
        )
```

## Verifying

While HTTP stubbing satisfies most needs, sometimes we want to assure that a certain request as made.

Different from WireMock, SireMock lets you match the same request object you created for stubbing your services:

```scala
val request = on(urlEqualTo("/hello")) get

request respond (aResponse withBody "hello, world!")

verify(request) wasCalled exactly(1)
```

Besides `exactly`, you can also use `lessThan`, `lessThanOrExactly`, `moreThanOrExactly` and `moreThan`.

## Stateful behavior

You can use the `Scenario` feature from WireMock using the `Scenario` SireMock object while building your request:

```scala
val helloWorldScenario = Scenario("hello, world")

(((on(urlEqualTo("/hello")) get) in (helloWorldScenario settingStateTo "once")) 
  respond (
    aResponse withBody """{"hello":"world-1"}"""
  )
)

(((on(urlEqualTo("/hello")) get) in (helloWorldScenario whenStateIs "once")) 
  respond (
    aResponse withBody """{"hello":"world-2"}"""
  )
)
```

## How can I help?

New PRs and new feature requests are very welcome, so, if you want to help with a new feature, create a new issue or send a PR.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


