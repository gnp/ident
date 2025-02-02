package com.gregorpurdy.ccs

import zio.test._
import zio.test.Assertion._

object IsoIec7064Test extends ZIOSpecDefault {

  def spec: Spec[Any, Any] = suite("IsoIec7064")(
    test("Max constant should have correct value") {
      val expected = (Long.MaxValue - Character.getNumericValue('Z')) / 100
      assert(expected)(equalTo(92233720368547757L)) &&
      assert(IsoIec7064.Max)(equalTo(expected)) // Will need private[ccs] modifier
    }
  )

}
