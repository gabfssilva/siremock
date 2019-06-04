package io.gabfssilva.siremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{CountMatchingStrategy, WireMock}
import com.github.tomakehurst.wiremock.matching._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import scala.collection.JavaConverters._
import scala.language.implicitConversions

trait ContentPatterns {
  def matching(str: String): StringValuePattern = WireMock.matching(str)
  def notMatching(str: String): StringValuePattern = WireMock.notMatching(str)
  def containing(str: String): StringValuePattern = WireMock.containing(str)
  def equalToXml(str: String): StringValuePattern = WireMock.equalToXml(str)
  def matchingJsonPath(str: String): StringValuePattern = WireMock.matchingJsonPath(str)
  def equalToJson(str: String, ignoreArrayOrder: Boolean = true, ignoreExtraElements: Boolean = true): StringValuePattern = WireMock.equalToJson(str, ignoreArrayOrder, ignoreExtraElements)
  def equalTo(str: String): StringValuePattern = WireMock.equalTo(str)
  def equalToIgnoreCase(str: String): StringValuePattern = WireMock.equalToIgnoreCase(str)
  def binaryEqualTo(str: String): BinaryEqualToPattern = WireMock.binaryEqualTo(str)
  def matchingXPath(str: String): StringValuePattern = WireMock.matchingXPath(str)
  def matchingXPath(str: String, valuePattern: StringValuePattern): StringValuePattern = WireMock.matchingXPath(str, valuePattern)
  def matchingXPath(str: String, namespaces: Map[String, String]): StringValuePattern = WireMock.matchingXPath(str, namespaces.asJava)
  def matchingJsonPath(str: String, valuePattern: StringValuePattern): StringValuePattern = WireMock.matchingJsonPath(str, valuePattern)
  def absent: StringValuePattern = WireMock.absent
}

trait CountMatchingStrategies {
  def lessThan(value: Int): CountMatchingStrategy = WireMock.lessThan(value)
  def lessThanOrExactly(value: Int): CountMatchingStrategy = WireMock.lessThanOrExactly(value)
  def exactly(value: Int): CountMatchingStrategy = WireMock.exactly(value)
  def moreThanOrExactly(value: Int): CountMatchingStrategy = WireMock.moreThanOrExactly(value)
  def moreThan(value: Int): CountMatchingStrategy = WireMock.moreThan(value)
}

trait UrlPatterns {
  def urlEqualTo(str: String): UrlPattern = WireMock.urlEqualTo(str)
  def urlPathEqualTo(str: String): UrlPattern = WireMock.urlPathEqualTo(str)
  def urlPathMatching(str: String): UrlPattern = WireMock.urlPathMatching(str)
  def urlMatching(str: String): UrlPattern = WireMock.urlMatching(str)
}

trait WireMockSupport
  extends ContentPatterns
    with CountMatchingStrategies
    with UrlPatterns
    with VerifySupport {

  val wireMockClient: WireMock

  def on(url: UrlPattern) = RequestBuilder(url = url)
  def aResponse = ResponseBuilder()

  def resetWireMock(): Unit = {
    wireMockClient.resetMappings()
    wireMockClient.resetRequests()
    wireMockClient.resetScenarios()
    wireMockClient.resetToDefaultMappings()
  }

  implicit class RegisterMappingImplicits(requestBuilder: RequestBuilder) {
    def respond(responseBuilder: ResponseBuilder): StubMapping =
      wireMockClient.register(Mapping(requestBuilder, responseBuilder).toMappingBuilder)

  }

  implicit class VerifyImplicits(verify: Verify) {
    def wasCalled(count: CountMatchingStrategy): Unit =
      wireMockClient.verifyThat(count, verify.requestMapping)
  }
}

trait SireMockClient extends WireMockSupport

trait SireMockServer extends WireMockSupport {
  val wireMockServer: WireMockServer

  def startWireMock(): Unit = wireMockServer.start()
  def stopWireMock(): Unit = wireMockServer.stop()

  implicit lazy val wireMockClient: WireMock = {
    val scheme = if (wireMockServer.getOptions.httpsSettings.enabled) "https" else "http"
    new WireMock(scheme, "localhost", wireMockServer.port())
  }
}
