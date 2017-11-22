package io.gabfssilva

import io.gabfssilva.siremock.{SireMock, SireMockConfig}

/**
  * @author Gabriel Francisco - gabfssilva@gmail.com
  */
trait SireMockSupport extends SireMock {
  override val sireMockConfig = SireMockConfig()
}

package object siremock extends SireMockSupport
