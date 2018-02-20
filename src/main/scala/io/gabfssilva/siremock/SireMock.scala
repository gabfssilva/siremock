package io.gabfssilva.siremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{BasicCredentials, CountMatchingStrategy, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders}
import com.github.tomakehurst.wiremock.matching._
import io.gabfssilva.siremock.HttpMethods._

import scala.language.implicitConversions
import scala.util.Try

object SireMock {
  def apply(config: SireMockConfig = SireMockConfig()): SireMock = new SireMock() {
    override val sireMockConfig = config
  }
}

trait Implicits {
  import scala.collection.JavaConverters._

  implicit class CountMatchingImplicits(value: Int) {
    def lessThanStrategy: CountMatchingStrategy = WireMock.lessThan(value)
    def lessThanOrExactlyStrategy: CountMatchingStrategy = WireMock.lessThanOrExactly(value)
    def exactlyStrategy: CountMatchingStrategy = WireMock.exactly(value)
    def moreThanOrExactlyStrategy: CountMatchingStrategy = WireMock.moreThanOrExactly(value)
    def moreThanStrategy: CountMatchingStrategy = WireMock.moreThan(value)
  }
  
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

  lazy val wireMockServer: WireMockServer = new WireMockServer(sireMockConfig.port)
  lazy val wireMockClient = new WireMock(sireMockConfig.port)

  def startSireMock = wireMockServer.start()
  def stopSireMock = wireMockServer.stop()
  def resetSireMock = wireMockServer.resetAll()

  private def requestPatternBuilder(initialBuilder: RequestPatternBuilder,
                                    headers: Map[String, StringValuePattern],
                                    contentType: Option[String],
                                    requestBodyMatching: StringValuePattern,
                                    basicAuth: Option[(String, String)]) = {
    def pattern =
      headers
        .foldLeft(initialBuilder) { case (builder, (k, v)) =>
          builder.withHeader(k, v)
        }
        .withRequestBody(requestBodyMatching)

    (contentType, basicAuth) match {
      case (Some(t), Some((username, password))) =>
        pattern
          .withHeader("Content-Type", t)
          .withBasicAuth(new BasicCredentials(username, password))

      case (Some(t), None) =>
        pattern
          .withHeader("Content-Type", t)

      case (None, Some((username, password))) =>
        pattern
          .withBasicAuth(new BasicCredentials(username, password))

      case (None, None) => pattern
    }
  }

  def verifyPost(path: UrlPattern,
                 count: CountMatchingStrategy = 1.exactlyStrategy,
                 headers: Map[String, StringValuePattern] = Map.empty,
                 contentType: Option[String] = None,
                 basicAuth: Option[(String, String)] = None,
                 requestBodyMatching: StringValuePattern = new AnythingPattern()) = {
    
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        postRequestedFor(path),
        headers,
        contentType,
        requestBodyMatching,
        basicAuth
      )
    )
  }

  def verifyPut(path: UrlPattern,
                count: CountMatchingStrategy = 1.exactlyStrategy,
                headers: Map[String, StringValuePattern] = Map.empty,
                contentType: Option[String] = None,
                basicAuth: Option[(String, String)] = None,
                requestBodyMatching: StringValuePattern = new AnythingPattern()) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        putRequestedFor(path),
        headers,
        contentType,
        requestBodyMatching,
        basicAuth
      )
    )
  }

  def verifyDelete(path: UrlPattern,
                count: CountMatchingStrategy = 1.exactlyStrategy,
                headers: Map[String, StringValuePattern] = Map.empty,
                contentType: Option[String] = None,
                basicAuth: Option[(String, String)] = None,
                requestBodyMatching: StringValuePattern = new AnythingPattern()) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        deleteRequestedFor(path),
        headers,
        contentType,
        requestBodyMatching,
        basicAuth
      )
    )
  }

  def verifyAny(path: UrlPattern,
                count: CountMatchingStrategy = 1.exactlyStrategy,
                headers: Map[String, StringValuePattern] = Map.empty,
                contentType: Option[String] = None,
                basicAuth: Option[(String, String)] = None,
                requestBodyMatching: StringValuePattern = new AnythingPattern()) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        anyRequestedFor(path),
        headers,
        contentType,
        requestBodyMatching,
        basicAuth
      )
    )
  }

  def verifyPatch(path: UrlPattern,
                  count: CountMatchingStrategy = 1.exactlyStrategy,
                  headers: Map[String, StringValuePattern] = Map.empty,
                  contentType: Option[String] = None,
                  basicAuth: Option[(String, String)] = None,
                  requestBodyMatching: StringValuePattern = new AnythingPattern()) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        patchRequestedFor(path),
        headers,
        contentType,
        requestBodyMatching,
        basicAuth
      )
    )
  }

  def verifyGet(path: UrlPattern,
                count: CountMatchingStrategy = 1.exactlyStrategy,
                headers: Map[String, StringValuePattern] = Map.empty,
                basicAuth: Option[(String, String)] = None,
                contentType: Option[String] = None) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        getRequestedFor(path),
        headers,
        contentType,
        new AnythingPattern(),
        basicAuth
      )
    )
  }

  def verifyTrace(path: UrlPattern,
                count: CountMatchingStrategy = 1.exactlyStrategy,
                headers: Map[String, StringValuePattern] = Map.empty,
                basicAuth: Option[(String, String)] = None,
                contentType: Option[String] = None) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        traceRequestedFor(path),
        headers,
        contentType,
        new AnythingPattern(),
        basicAuth
      )
    )
  }

  def verifyHead(path: UrlPattern,
                 count: CountMatchingStrategy = 1.exactlyStrategy,
                 headers: Map[String, StringValuePattern] = Map.empty,
                 basicAuth: Option[(String, String)] = None,
                 contentType: Option[String] = None) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        headRequestedFor(path),
        headers,
        contentType,
        new AnythingPattern(),
        basicAuth
      )
    )
  }

  def verifyOptions(path: UrlPattern,
                    count: CountMatchingStrategy = 1.exactlyStrategy,
                    headers: Map[String, StringValuePattern] = Map.empty,
                    basicAuth: Option[(String, String)] = None,
                    contentType: Option[String] = None) = {
    wireMockClient.verifyThat(count,
      requestPatternBuilder(
        optionsRequestedFor(path),
        headers,
        contentType,
        new AnythingPattern(),
        basicAuth
      )
    )
  }

  def mockPost(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              requestBodyMatching: StringValuePattern = new AnythingPattern(),
              withResponseBody: Option[String] = None,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withBasicAuth: Option[(String, String)] = None,
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
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockPut(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: Option[String] = None,
               requestBodyMatching: StringValuePattern = new AnythingPattern(),
               withResponseBody: Option[String] = None,
               withResponseStatus: Int = 200,
               withResponseHeaders: Map[String, List[String]] = Map.empty,
               withBasicAuth: Option[(String, String)] = None,
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
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockPatch(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: Option[String] = None,
               requestBodyMatching: StringValuePattern = new AnythingPattern(),
               withResponseBody: Option[String] = None,
               withResponseStatus: Int = 200,
               withResponseHeaders: Map[String, List[String]] = Map.empty,
               withBasicAuth: Option[(String, String)] = None,
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
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockDelete(path: UrlPattern,
               headers: Map[String, StringValuePattern] = Map.empty,
               contentType: Option[String] = None,
               requestBodyMatching: StringValuePattern = new AnythingPattern(),
               withResponseBody: Option[String] = None,
               withResponseStatus: Int = 200,
               withResponseHeaders: Map[String, List[String]] = Map.empty,
               withBasicAuth: Option[(String, String)] = None,
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
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockGet(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: Option[String] = None,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withBasicAuth: Option[(String, String)] = None,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = GET,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockOptions(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: Option[String] = None,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withBasicAuth: Option[(String, String)] = None,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = OPTIONS,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockHead(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: Option[String] = None,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withBasicAuth: Option[(String, String)] = None,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = HEAD,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockTrace(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: Option[String] = None,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withBasicAuth: Option[(String, String)] = None,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = TRACE,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockAny(path: UrlPattern,
              headers: Map[String, StringValuePattern] = Map.empty,
              contentType: Option[String] = None,
              withResponseBody: Option[String] = None,
              withResponseStatus: Int = 200,
              withResponseHeaders: Map[String, List[String]] = Map.empty,
              withBasicAuth: Option[(String, String)] = None,
              withFixedDelay: Int = 0) =
    mockWithoutEntity(
      path = path,
      method = ANY,
      headers = headers,
      contentType = contentType,
      withResponseBody = withResponseBody,
      withResponseStatus = withResponseStatus,
      withResponseHeaders = withResponseHeaders,
      withFixedDelay = withFixedDelay,
      withBasicAuth = withBasicAuth
    )

  def mockWithEntity(path: UrlPattern,
                     method: HttpMethod,
                     headers: Map[String, StringValuePattern] = Map.empty,
                     contentType: Option[String] = None,
                     requestBodyMatching: StringValuePattern = new AnythingPattern(),
                     withResponseBody: Option[String] = None,
                     withResponseStatus: Int = 200,
                     withResponseHeaders: Map[String, List[String]] = Map.empty,
                     withBasicAuth: Option[(String, String)],
                     withFixedDelay: Int = 0) = {
    wireMockServer.stubFor {
      headers
        .foldLeft(initialRequest(path, method, contentType, withBasicAuth)) { case (previous, (k, v)) => previous.withHeader(k, v) }
        .withRequestBody(requestBodyMatching)
        .willReturn(responseBuilder(withResponseBody, withResponseStatus, withResponseHeaders, withFixedDelay))
    }
  }

  def mockWithoutEntity(path: UrlPattern,
                        method: HttpMethod,
                        headers: Map[String, StringValuePattern] = Map.empty,
                        contentType: Option[String] = None,
                        withResponseBody: Option[String] = None,
                        withResponseStatus: Int = 200,
                        withResponseHeaders: Map[String, List[String]] = Map.empty,
                        withBasicAuth: Option[(String, String)],
                        withFixedDelay: Int = 0) = {
    wireMockServer.stubFor {
      headers
        .foldLeft(initialRequest(path, method, contentType, withBasicAuth)) { case (previous, (k, v)) => previous.withHeader(k, v) }
        .willReturn(responseBuilder(withResponseBody, withResponseStatus, withResponseHeaders, withFixedDelay))
    }
  }

  private def initialRequest(path: UrlPattern, method: HttpMethod, contentType: Option[String] = None, basicAuth: Option[(String, String)]) = {
    (contentType, basicAuth) match {
      case (Some(ct), Some((username, password))) =>
        request(method.toString, path)
          .withHeader("Content-Type", ct)
          .withBasicAuth(username, password)

      case (None, Some((username, password))) =>
        request(method.toString, path)
          .withBasicAuth(username, password)

      case (Some(ct), None) =>
        request(method.toString, path)
          .withHeader("Content-Type", ct)

      case (None, None) =>
        request(method.toString, path)
    }
  }

  private def responseBuilder(withResponseBody: Option[String] = None,
                              withResponseStatus: Int,
                              withResponseHeaders: Map[String, List[String]] = Map.empty,
                              withFixedDelay: Int) = {
    import scala.collection.JavaConverters._

    val response = aResponse()
      .withStatus(withResponseStatus)
      .withFixedDelay(withFixedDelay)
      .withHeaders(new HttpHeaders(withResponseHeaders.map { case (k, v) => new HttpHeader(k, v: _*) }.asJava))

    withResponseBody match {
      case None => response
      case Some(body) => response.withBody(body)
    }
  }
}
