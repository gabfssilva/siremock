package io.gabfssilva.siremock

import com.github.tomakehurst.wiremock.client.{BasicCredentials, WireMock}
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder

case class Verify(request: RequestBuilder) {
  def requestMapping: RequestPatternBuilder = {
    var builder = request.method match {
      case HttpMethods.POST => WireMock.postRequestedFor(request.url)
      case HttpMethods.GET => WireMock.getRequestedFor(request.url)
      case HttpMethods.ANY => WireMock.anyRequestedFor(request.url)
      case HttpMethods.TRACE => WireMock.traceRequestedFor(request.url)
      case HttpMethods.HEAD => WireMock.headRequestedFor(request.url)
      case HttpMethods.PATCH => WireMock.patchRequestedFor(request.url)
      case HttpMethods.OPTIONS => WireMock.optionsRequestedFor(request.url)
      case HttpMethods.PUT => WireMock.putRequestedFor(request.url)
      case HttpMethods.DELETE => WireMock.deleteRequestedFor(request.url)
    }

    builder = request.authorization match {
      case Some(BasicAuth(u, p)) => builder.withBasicAuth(new BasicCredentials(u, p))
      case Some(BearerToken(t)) => builder.withHeader("Authorization", WireMock.equalTo(s"Bearer $t"))
      case None => builder
    }

    request.headers.foreach { case (k, v) => builder = builder.withHeader(k, v) }

    builder = request.body match {
      case None => builder
      case Some(body) => builder.withRequestBody(body)
    }

    builder = request.contentType match {
      case Some(ct) => builder.withHeader("Content-Type", WireMock.equalTo(ct))
      case None => builder
    }

    builder
  }
}

trait VerifySupport {
  def verify(request: RequestBuilder) = Verify(request)
}
