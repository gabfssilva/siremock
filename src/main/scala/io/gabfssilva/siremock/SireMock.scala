package io.gabfssilva.siremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.{BinaryEqualToPattern, StringValuePattern, UrlPattern}

import scala.language.implicitConversions

object SireMock {
  def apply(config: SireMockConfig = SireMockConfig()): SireMock = new SireMock() {
    override val sireMockConfig = config
  }
}

sealed trait HttpMethod

object HttpMethods {
  object POST extends HttpMethod
  object GET extends HttpMethod
  object PUT extends HttpMethod
  object PATCH extends HttpMethod
  object DELETE extends HttpMethod
  object HEAD extends HttpMethod
  object TRACE extends HttpMethod
  object OPTIONS extends HttpMethod

  def fromString(method: String): HttpMethod = {
    method match {
      case "GET" | "get" => GET
      case "POST" | "post" => POST
      case "PUT" | "put" => PUT
      case "PATCH" | "patch" => PATCH
      case "DELETE" | "delete" => DELETE
      case "HEAD" | "head" => HEAD
      case "TRACE" | "trace" => TRACE
      case "OPTIONS" | "options" => OPTIONS
    }
  }
}

trait Implicits {
  implicit class StringValuePatternImplicits(str: String) {
    def matching: StringValuePattern = WireMock.matching(str)
    def notMatching: StringValuePattern = WireMock.notMatching(str)
    def containing: StringValuePattern = WireMock.containing(str)
    def equalToXml: StringValuePattern = WireMock.equalToXml(str)
    def matchingJsonPath: StringValuePattern = WireMock.matchingJsonPath(str)
    def equalToJson: StringValuePattern = WireMock.equalToJson(str)
    def equalTo: StringValuePattern = WireMock.equalTo(str)
    def equalToIgnoreCase: StringValuePattern = WireMock.equalToIgnoreCase(str)
    def binaryEqualTo: BinaryEqualToPattern = WireMock.binaryEqualTo(str)
  }

  implicit class UrlPatternImplicits(str: String) {
    def urlEqualTo: UrlPattern = WireMock.urlEqualTo(str)
    def urlPathEqualTo: UrlPattern = WireMock.urlPathEqualTo(str)
    def urlPathMatching: UrlPattern = WireMock.urlPathMatching(str)
    def urlMatching: UrlPattern = WireMock.urlMatching(str)
  }

  implicit def defaultStringValuePattern(str: String): StringValuePattern = str.equalTo

  implicit def stringToHttpMethod(method: String): HttpMethod = HttpMethods.fromString(method)
}

trait SireMock extends Implicits{
  val sireMockConfig: SireMockConfig
  val wireMockServer = new WireMockServer(sireMockConfig.port)

  def mockPost(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: String,
               requestBodyMatching: StringValuePattern) = {
    wireMockServer.stubFor {
      headers
        .foldLeft(post(path)) { case (previous, (k, v)) =>  previous.withHeader(k, v) }
        .withHeader("Content-Type", contentType)
        .withRequestBody(requestBodyMatching)
    }
  }
}
