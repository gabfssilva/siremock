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

      (on(urlEqualTo("/hello")) get) returning (aResponse withBody expectedResponseBody)

      val resp = Http("http://localhost:" + port + "/hello").method("get").asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
    }

    scenario("basic verifying") {
      val expectedResponseBody = """{"hello":"world"}"""

      val request = on(urlEqualTo("/hello-verified")) get

      request.returning(aResponse withBody expectedResponseBody)

      val resp = Http("http://localhost:" + port + "/hello-verified").method("get").asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200

      verify(request) wasCalled 1
    }

    scenario("basic authentication") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello-auth"))
        .get
        .withAuth(BasicAuth("user", "pass"))
        .returning(aResponse.withBody(expectedResponseBody))

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
        .returning(aResponse.withBody(expectedResponseBody))

      val resp = Http("http://localhost:" + port + "/hello-auth")
        .method("get")
        .header("Authorization", s"Bearer $token")
        .asString

      resp.body shouldBe expectedResponseBody
      resp.code shouldBe 200
    }
  }

  feature("POST") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      on(urlEqualTo("/hello"))
        .post
        .withBody(equalToJson("""{"hi":"you"}"""))
        .withContentType("application/json")
        .returning(
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
        .returning(
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
        .returning(aResponse.withBody(expectedResponseBody))

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
        .returning(aResponse.withBody(expectedResponseBody))

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
