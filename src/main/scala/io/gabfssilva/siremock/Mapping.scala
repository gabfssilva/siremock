package io.gabfssilva.siremock

import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.http.{Fault, HttpHeader, HttpHeaders}
import com.github.tomakehurst.wiremock.matching.{ContentPattern, StringValuePattern, UrlPattern}

import scala.concurrent.duration.FiniteDuration
import scala.collection.JavaConverters._

sealed trait Authorization

case class BasicAuth(username: String, password: String) extends Authorization
case class BearerToken(token: String) extends Authorization

case class RequestBuilder(private[siremock] val url: UrlPattern,
                          private[siremock] val contentType: Option[String] = None,
                          private[siremock] val method: HttpMethod = HttpMethods.GET,
                          private[siremock] val body: Option[ContentPattern[_]] = None,
                          private[siremock] val headers: Map[String, StringValuePattern] = Map.empty,
                          private[siremock] val authorization: Option[Authorization] = None) {
  def post: RequestBuilder = copy(method = HttpMethods.POST)
  def get: RequestBuilder = copy(method = HttpMethods.GET)
  def put: RequestBuilder = copy(method = HttpMethods.PUT)
  def patch: RequestBuilder = copy(method = HttpMethods.PATCH)
  def delete: RequestBuilder = copy(method = HttpMethods.DELETE)
  def options: RequestBuilder = copy(method = HttpMethods.OPTIONS)
  def head: RequestBuilder = copy(method = HttpMethods.HEAD)
  def trace: RequestBuilder = copy(method = HttpMethods.TRACE)
  def any: RequestBuilder = copy(method = HttpMethods.ANY)

  def withMethod(v: HttpMethod): RequestBuilder = copy(method = v)
  def withContentType(contentType: String): RequestBuilder = copy(contentType = Some(contentType))
  def withBody(v: ContentPattern[_]): RequestBuilder = copy(body = Some(v))
  def withHeaders(headers: (String, StringValuePattern)*): RequestBuilder = copy(headers = headers.toMap)
  def withAuth(auth: Authorization): RequestBuilder = copy(authorization = Some(auth))
}

case class ResponseBuilder(private[siremock] val status: Int = 200,
                           private[siremock] val body: Option[String] = None,
                           private[siremock] val headers: Map[String, String] = Map.empty,
                           private[siremock] val delayInMs: Long = 0) {
  def withStatus(status: Int): ResponseBuilder = copy(status = status)
  def withBody(body: String): ResponseBuilder = copy(body = Some(body))
  def withHeaders(headers: (String, String)*): ResponseBuilder = copy(headers = headers.toMap)
  def withDelay(duration: FiniteDuration): ResponseBuilder = copy(delayInMs = duration.toMillis)
}

case class Mapping(request: RequestBuilder,
                   response: ResponseBuilder) {
  def toMappingBuilder: MappingBuilder = {
    var builder =
      WireMock
        .request(request.method.stringValue, request.url)

    builder = request.body match {
      case Some(body) => builder.withRequestBody(body)
      case None       => builder
    }

    request.headers.foreach { case (k, v) => builder = builder.withHeader(k, v) }

    builder = request.contentType match {
      case Some(ct) => builder.withHeader("Content-Type", WireMock.equalTo(ct))
      case None     => builder
    }

    builder = request.authorization match {
      case Some(BasicAuth(u, p)) => builder.withBasicAuth(u, p)
      case Some(BearerToken(t))  => builder.withHeader("Authorization", WireMock.equalTo(s"Bearer $t"))
      case None                  => builder
    }

    builder.willReturn(
      WireMock
        .aResponse()
        .withStatus(response.status)
        .withBody(response.body.getOrElse(""))
        .withFixedDelay(response.delayInMs.toInt)
        .withHeaders(new HttpHeaders(response.headers.map { case (k, v) => new HttpHeader(k, v) }.asJava))
    )

    builder
  }
}
