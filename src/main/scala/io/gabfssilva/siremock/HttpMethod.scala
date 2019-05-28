package io.gabfssilva.siremock

sealed abstract class HttpMethod(val stringValue: String, val entityRequest: Boolean = false) {
  override def toString: String = stringValue
}

object HttpMethods {
  case object POST extends HttpMethod("POST", true)
  case object GET extends HttpMethod("GET")
  case object PUT extends HttpMethod("PUT", true)
  case object PATCH extends HttpMethod("PATCH", true)
  case object DELETE extends HttpMethod("DELETE", true)
  case object HEAD extends HttpMethod("HEAD")
  case object TRACE extends HttpMethod("TRACE")
  case object OPTIONS extends HttpMethod("OPTIONS")
  case object ANY extends HttpMethod("ANY")

  def fromString(method: String): HttpMethod = {
    method match {
      case m if m.equalsIgnoreCase("get")     => GET
      case m if m.equalsIgnoreCase("post")    => POST
      case m if m.equalsIgnoreCase("put")     => PUT
      case m if m.equalsIgnoreCase("patch")   => PATCH
      case m if m.equalsIgnoreCase("delete")  => DELETE
      case m if m.equalsIgnoreCase("head")    => HEAD
      case m if m.equalsIgnoreCase("trace")   => TRACE
      case m if m.equalsIgnoreCase("options") => OPTIONS
      case m if m.equalsIgnoreCase("any")     => ANY
    }
  }
}

