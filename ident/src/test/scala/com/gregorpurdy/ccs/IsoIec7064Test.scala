package com.gregorpurdy.ccs

import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*

class IsoIec7064Test extends AnyFunSpec with should.Matchers {

  describe("IsoIec7064") {
    it("Max constant should have correct value") {
      val expected = (Long.MaxValue - Character.getNumericValue('Z')) / 100
      expected shouldBe 92233720368547757L
      IsoIec7064.Max shouldBe expected
    }
  }

}
