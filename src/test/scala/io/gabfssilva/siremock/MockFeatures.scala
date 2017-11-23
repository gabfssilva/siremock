package io.gabfssilva.siremock

import io.gabfssilva.SireMockSupport
import org.scalatest.{BeforeAndAfter, FeatureSpec, Matchers}

import scalaj.http.{Http, StringBodyConnectFunc}

class MockFeatures
  extends FeatureSpec
    with Matchers
    with SireMockSupport
    with BeforeAndAfter {

  override val sireMockConfig: SireMockConfig = SireMockConfig(port = 8181)

  before {
    resetAll
  }

  feature("GET") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      mockGet(
        path = "/hello",
        withResponseBody = expectedResponseBody
      )

      val response = Http("http://localhost:8181/hello")
        .method("get")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200
    }
  }

  feature("POST") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      mockPost(
        path = "/hello",
        requestBodyMatching = """{"hi":"you"}""",
        contentType = Some("application/json"),
        withResponseBody = expectedResponseBody,
        withResponseStatus = 201
      )

      val response = Http("http://localhost:8181/hello")
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

      mockPut(
        path = "/hello",
        requestBodyMatching = """{"hi":"you"}""",
        contentType = Some("application/json"),
        withResponseBody = expectedResponseBody
      )

      val response = Http("http://localhost:8181/hello")
        .header("Content-Type", "application/json")
        .put("""{"hi":"you"}""")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200
    }
  }

  feature("DELETE") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      mockDelete(
        path = "/hello",
        withResponseBody = expectedResponseBody
      )

      val response = Http("http://localhost:8181/hello").method("delete")
//        .copy(connectFunc=StringBodyConnectFunc(""))
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200
    }
  }

  feature("PATCH") {
    scenario("basic mocking") {
      val expectedResponseBody = """{"hello":"world"}"""

      mockPatch(
        path = "/hello",
        requestBodyMatching = """{"hi":"you"}""",
        contentType = Some("application/json"),
        withResponseBody = expectedResponseBody,
      )

      val response = Http("http://localhost:8181/hello")
        .header("Content-Type", "application/json")
        .copy(connectFunc=StringBodyConnectFunc("""{"hi":"you"}"""))
        .method("PATCH")
        .asString

      response.body shouldBe expectedResponseBody
      response.code shouldBe 200
    }
  }
}
