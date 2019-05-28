package io.gabfssilva.siremock

import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.{BeforeAndAfter, FeatureSpec, Matchers}
import scalaj.http.{Http, StringBodyConnectFunc}

import scala.language.postfixOps

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

      (on(urlEqualTo("/hello")) get) respond (aResponse withBody expectedResponseBody)

      val resp = Http("http://localhost:" + port + "/hello").method("get").asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
    }

    scenario("basic verifying") {
      val expectedResponseBody = """{"hello":"world"}"""

      val request = on(urlEqualTo("/hello-verified")) get

      request.respond(aResponse withBody expectedResponseBody withContentType "application/json")

      val resp = Http("http://localhost:" + port + "/hello-verified").method("get").asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
      resp.contentType shouldBe Some("application/json")

      verify(request) wasCalled exactly(1)
    }

    scenario("basic authentication") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello-auth"))
        .get
        .withAuth(BasicAuth("user", "pass"))
        .respond(aResponse.withBody(expectedResponseBody))

      val resp = Http("http://localhost:" + port + "/hello-auth")
        .method("get")
        .auth("user", "pass")
        .asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
    }

    scenario("token authentication") {
      val token = UUID.randomUUID().toString.replace("-", "")

      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello-auth"))
        .get
        .withAuth(BearerToken(token))
        .respond(aResponse.withBody(expectedResponseBody))

      val resp = Http("http://localhost:" + port + "/hello-auth")
        .method("get")
        .header("Authorization", s"Bearer $token")
        .asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
    }

    scenario("basic with state") {
      val expectedResponseBody1 = """{"hello":"world-1"}"""
      val expectedResponseBody2 = """{"hello":"world-2"}"""
      val expectedResponseBody3 = """{"hello":"world-3"}"""

      val helloWorldScenario = Scenario("hello, world")

      ((on(urlEqualTo("/hello")) get) in (helloWorldScenario settingStateTo "once")) respond (aResponse withBody expectedResponseBody1)
      ((on(urlEqualTo("/hello")) get) in (helloWorldScenario whenStateIs "once" settingStateTo "twiceOrMore")) respond (aResponse withBody expectedResponseBody2)
      ((on(urlEqualTo("/hello")) get) in (helloWorldScenario whenStateIs "twiceOrMore")) respond (aResponse withBody expectedResponseBody3)

      Http("http://localhost:" + port + "/hello").method("get").asString.body shouldBe expectedResponseBody1
      Http("http://localhost:" + port + "/hello").method("get").asString.body shouldBe expectedResponseBody2
      Http("http://localhost:" + port + "/hello").method("get").asString.body shouldBe expectedResponseBody3
      Http("http://localhost:" + port + "/hello").method("get").asString.body shouldBe expectedResponseBody3
      Http("http://localhost:" + port + "/hello").method("get").asString.body shouldBe expectedResponseBody3
    }
  }

  feature("POST") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello"))
        .post
        .withBody(equalToJson("""{"hi":"you"}"""))
        .withContentType("application/json")
        .respond(
          aResponse
            .withBody(expectedResponseBody)
            .withStatus(201)
        )

      val response = Http("http://localhost:" + port + "/hello")
        .header("Content-Type", "application/json")
        .postData("""{"hi":"you"}""")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 201
    }
  }

  feature("PUT") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello"))
        .put
        .withBody(equalToJson("""{"hi":"you"}"""))
        .withContentType("application/json")
        .respond(
          aResponse
            .withBody(expectedResponseBody)
            .withStatus(201)
        )

      val response = Http("http://localhost:" + port + "/hello")
        .header("Content-Type", "application/json")
        .put("""{"hi":"you"}""")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 201
    }
  }

  feature("DELETE") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello"))
        .delete
        .respond(aResponse.withBody(expectedResponseBody))

      val response =
        Http("http://localhost:" + port + "/hello")
          .method("delete")
          .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200
    }
  }

  feature("PATCH") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello"))
        .patch
        .withBody(equalToJson("""{"hi":"you"}"""))
        .respond(aResponse.withBody(expectedResponseBody))

      val response = Http("http://localhost:" + port + "/hello")
        .header("Content-Type", "application/json")
        .copy(connectFunc = StringBodyConnectFunc("""{"hi":"you"}"""))
        .method("PATCH")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200
    }
  }
}
