package io.gabfssilva.siremock

import io.gabfssilva.SireMockSupport
import org.scalatest.{BeforeAndAfter, FeatureSpec, Matchers}

import scalaj.http.Http

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
}
