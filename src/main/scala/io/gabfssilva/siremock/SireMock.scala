package io.gabfssilva.siremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders}
import com.github.tomakehurst.wiremock.matching.{BinaryEqualToPattern, StringValuePattern, UrlPattern}
import io.gabfssilva.siremock.HttpMethods._

import scala.language.implicitConversions

object SireMock {
  def apply(config: SireMockConfig = SireMockConfig()): SireMock = new SireMock() {
    override val sireMockConfig = config
  }
}

trait Implicits {
  import scala.collection.JavaConverters._
  
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
    def matchingXPath: StringValuePattern = WireMock.matchingXPath(str)
    def matchingXPath(valuePattern: StringValuePattern): StringValuePattern = WireMock.matchingXPath(str, valuePattern)
    def matchingXPath(namespaces: Map[String, String]): StringValuePattern = WireMock.matchingXPath(str, namespaces.asJava)
    def matchingJsonPath(valuePattern: StringValuePattern): StringValuePattern = WireMock.matchingJsonPath(str, valuePattern)
    def equalToJson(ignoreArrayOrder: Boolean, ignoreExtraElements: Boolean): StringValuePattern = WireMock.equalToJson(str, ignoreArrayOrder, ignoreExtraElements)
    def absent: StringValuePattern = WireMock.absent
  }
  
  implicit class UrlPatternImplicits(str: String) {
    def urlEqualTo: UrlPattern = WireMock.urlEqualTo(str)
    def urlPathEqualTo: UrlPattern = WireMock.urlPathEqualTo(str)
    def urlPathMatching: UrlPattern = WireMock.urlPathMatching(str)
    def urlMatching: UrlPattern = WireMock.urlMatching(str)
  }

  implicit def defaultUrlPattern(str: String): UrlPattern = str.urlEqualTo
  implicit def defaultStringValuePattern(str: String): StringValuePattern = str.equalTo
  implicit def stringToHttpMethod(method: String): HttpMethod = HttpMethods.fromString(method)
}

trait SireMock extends Implicits {
  val sireMockConfig: SireMockConfig

  lazy val wireMockServer: WireMockServer = {
    val s = new WireMockServer(sireMockConfig.port)
    s.start()
    s
  }

  def resetAll = {
    wireMockServer.resetAll()
  }


  def mockPost(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              requestBodyMatching: StringValuePattern,
              withResponseBody: String,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withFixedDelay: Int = 0) =
    mockWithEntity(
      path = path,
      method = POST,
      headers = headers,
      contentType = contentType,
      requestBodyMatching = requestBodyMatching,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockPut(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: Option[String] = None,
               requestBodyMatching: StringValuePattern,
               withResponseBody: String,
               withResponseStatus: Int = 200,
               withResponseHeaders: Map[String, List[String]] = Map.empty,
               withFixedDelay: Int = 0) =
    mockWithEntity(
      path = path,
      method = PUT,
      headers = headers,
      contentType = contentType,
      requestBodyMatching = requestBodyMatching,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockPatch(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: Option[String] = None,
               requestBodyMatching: StringValuePattern,
               withResponseBody: String,
               withResponseStatus: Int = 200,
               withResponseHeaders: Map[String, List[String]] = Map.empty,
               withFixedDelay: Int = 0) =
    mockWithEntity(
      path = path,
      method = PATCH,
      headers = headers,
      contentType = contentType,
      requestBodyMatching = requestBodyMatching,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockDelete(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: Option[String] = None,
               requestBodyMatching: StringValuePattern,
               withResponseBody: String,
               withResponseStatus: Int = 200,
               withResponseHeaders: Map[String, List[String]] = Map.empty,
               withFixedDelay: Int = 0) =
    mockWithEntity(
      path = path,
      method = DELETE,
      headers = headers,
      contentType = contentType,
      requestBodyMatching = requestBodyMatching,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockGet(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: String,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = GET,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockOptions(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: String,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = OPTIONS,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockHead(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: String,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = HEAD,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockTrace(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: String,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = TRACE,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockAny(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: String,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = ANY,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay
    )

  def mockWithEntity(path: UrlPattern,
                     method: HttpMethod,
                     headers: Map[String, StringValuePattern] = Map.empty,
                     contentType: Option[String] = None,
                     requestBodyMatching: StringValuePattern,
                     withResponseBody: String,
                     withResponseStatus: Int = 200,
                     withResponseHeaders: Map[String, List[String]] = Map.empty,
                     withFixedDelay: Int = 0) = {
    wireMockServer.stubFor {
      headers
        .foldLeft(initialRequest(path, method, contentType)) { case (previous, (k, v)) => previous.withHeader(k, v) }
        .withRequestBody(requestBodyMatching)
        .willReturn(responseBuilder(withResponseBody, withResponseStatus, withResponseHeaders, withFixedDelay))
    }
  }

  def mockWithoutEntity(path: UrlPattern,
                        method: HttpMethod,
                        headers: Map[String, StringValuePattern] = Map.empty,
                        contentType: Option[String] = None,
                        withResponseBody: String,
                        withResponseStatus: Int = 200,
                        withResponseHeaders: Map[String, List[String]] = Map.empty,
                        withFixedDelay: Int = 0) = {
    wireMockServer.stubFor {
      headers
        .foldLeft(initialRequest(path, method, contentType)) { case (previous, (k, v)) => previous.withHeader(k, v) }
        .willReturn(responseBuilder(withResponseBody, withResponseStatus, withResponseHeaders, withFixedDelay))
    }
  }

  private def initialRequest(path: UrlPattern, method: HttpMethod, contentType: Option[String] = None) = {
    contentType
      .map { ct => request(method.toString, path).withHeader("Content-Type", ct) }
      .getOrElse(request(method.toString, path))
  }

  private def responseBuilder(withResponseBody: String,
                              withResponseStatus: Int,
                              withResponseHeaders: Map[String, List[String]] = Map.empty,
                              withFixedDelay: Int) = {
    import scala.collection.JavaConverters._

    aResponse()
      .withStatus(withResponseStatus)
      .withBody(withResponseBody)
      .withFixedDelay(withFixedDelay)
      .withHeaders(new HttpHeaders(withResponseHeaders.map { case (k, v) => new HttpHeader(k, v: _*) }.asJava))
  }
}
