package com.gregorpurdy.ccs

import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalacheck.Test
import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*

class Modulus10DoubleAddDoubleSpec extends AnyFunSpec with should.Matchers {

  import Modulus10DoubleAddDouble.*

  def genAlphaNumStrOfLength(length: Int): Gen[String] =
    Gen.listOfN(length, Gen.alphaNumChar).map(_.mkString)

  def genAlphaNumStr(minLength: Int, maxLength: Int): Gen[String] =
    for {
      length <- Gen.choose(minLength, maxLength)
      str <- Gen.listOfN(length, Gen.alphaNumChar).map(_.mkString)
    } yield str

  describe("Modulus10DoubleAddDouble") {

    it("CusipVariant on CUSIP-length (8) payloads") {
      forAll(genAlphaNumStrOfLength(8)) { s =>
        val payload = s.toUpperCase
        val a = CusipVariant.calculate(payload)
        val b = CusipVariant.calculateSimple(payload)
        a.equals(b)
      }.check(Test.Parameters.default.withMinSuccessfulTests(1000))
    }

    it("CusipVariant on FIGI-length (11) payloads") {
      forAll(genAlphaNumStrOfLength(11)) { s =>
        val payload = s.toUpperCase
        val a = CusipVariant.calculate(payload)
        val b = CusipVariant.calculateSimple(payload)
        a.equals(b)
      }.check(Test.Parameters.default.withMinSuccessfulTests(1000))
    }

    it("CusipVariant on length 1 payloads") {
      forAll(genAlphaNumStrOfLength(1)) { s =>
        val payload = s.toUpperCase
        val a = CusipVariant.calculate(payload)
        val b = CusipVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("CusipVariant on length 2 payloads") {
      forAll(genAlphaNumStrOfLength(2)) { s =>
        val payload = s.toUpperCase
        val a = CusipVariant.calculate(payload)
        val b = CusipVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("CusipVariant on length 1-100 payloads") {
      forAll(genAlphaNumStr(1, 100)) { s =>
        val payload = s.toUpperCase
        val a = CusipVariant.calculate(payload)
        val b = CusipVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("IsinVariant on ISIN-length (11) payloads") {
      forAll(genAlphaNumStrOfLength(11)) { s =>
        val payload = s.toUpperCase
        val a = IsinVariant.calculate(payload)
        val b = IsinVariant.calculateSimple(payload)
        a.equals(b)
      }.check(Test.Parameters.default.withMinSuccessfulTests(1000))
    }

    it("IsinVariant on length 1 payloads") {
      forAll(genAlphaNumStrOfLength(1)) { s =>
        val payload = s.toUpperCase
        val a = IsinVariant.calculate(payload)
        val b = IsinVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("IsinVariant on length 2 payloads") {
      forAll((genAlphaNumStrOfLength(2))) { s =>
        val payload = s.toUpperCase
        val a = IsinVariant.calculate(payload)
        val b = IsinVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("IsinVariant on length 1-100 payloads [Uppercase]") {
      forAll(genAlphaNumStr(1, 100)) { s =>
        val payload = s.toUpperCase
        val a = IsinVariant.calculate(payload)
        val b = IsinVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("IsinVariant on length 1-100 payloads [Lowercase]") {
      forAll(genAlphaNumStr(1, 100)) { s =>
        val payload = s.toLowerCase
        val a = IsinVariant.calculate(payload)
        val b = IsinVariant.calculateSimple(payload)
        a.equals(b)
      }
    }

    it("charValue should return zero for non-alphanumeric characters") {
      charValue('#') shouldBe 0
    }

  }

}
