package io.gabfssilva.siremock

sealed abstract class HttpMethod(val stringValue: String, val entityRequest: Boolean = false) {
  override def toString: String = stringValue
}

object HttpMethods {
  object POST extends HttpMethod("POST", true)
  object GET extends HttpMethod("GET")
  object PUT extends HttpMethod("PUT", true)
  object PATCH extends HttpMethod("PATCH", true)
  object DELETE extends HttpMethod("DELETE", true)
  object HEAD extends HttpMethod("HEAD")
  object TRACE extends HttpMethod("TRACE")
  object OPTIONS extends HttpMethod("OPTIONS")
  object ANY extends HttpMethod("ANY")

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
      case "ANY" | "any" => ANY
    }
  }
}

