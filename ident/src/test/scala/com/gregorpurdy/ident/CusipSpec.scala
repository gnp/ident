package com.gregorpurdy.ident

import zio.Chunk
import zio.test.Assertion.*
import zio.test.*

object CusipSpec extends ZIOSpecDefault {

  val issuerNumber = "037833"
  val issueNumber = "10"
  val checkDigit = "0"
  val payload = s"$issuerNumber$issueNumber"
  val cusipString = s"$payload$checkDigit"

  def spec: Spec[Any, Any] = suite("CusipSpec")(
    test("Correctly validate the AAPL CUSIP Issuer number") {
      val result = Cusip.validateIssuerFormat(issuerNumber)
      assert(result)(isRight(equalTo(issuerNumber)))
    },
    test("Correctly validate the AAPL CUSIP Issue number") {
      val result = Cusip.validateIssueFormat(issueNumber)
      assert(result)(isRight(equalTo(issueNumber)))
    },
    test("Correctly validate the AAPL CUSIP payload") {
      val result = Cusip.validatePayloadFormat(payload)
      assert(result)(isRight(equalTo(payload)))
    },
    test("Correctly validate the AAPL CUSIP Check Digit") {
      val result = Cusip.validateCheckDigitFormat(checkDigit)
      assert(result)(isRight(equalTo(checkDigit)))
    },
    test("Correctly return an error when validating Check Digit '1' for Issuer '111111' and Issue '11'") {
      val expected = CusipError.IncorrectCheckDigitValue("1", "8", "111111", "11")
      val result = Cusip.validateCheckDigitForPartsInternal("111111", "11", "1")
      assert(result)(isLeft(equalTo(expected)))
    },
    test("Correctly return an error when parsing '111111111'") {
      val expected = CusipError.IncorrectCheckDigitValue("1", "8", "111111", "11")
      val result = Cusip.fromString("111111111")
      assert(result)(isLeft(equalTo(expected)))
    },
    test("Correctly validate the example AAPL CUSIP") {
      assert(Cusip.validateFormat("\t" + cusipString))(isRight(equalTo(cusipString)))
      assert(Cusip.isValidFormat("\t" + cusipString))(isTrue)
    },
    test("Correctly validate the example AAPL CUSIP [STRICT]") {
      assert(Cusip.validateFormatStrict(cusipString))(isRight(equalTo(cusipString)))
      assert(Cusip.isValidFormatStrict(cusipString))(isTrue)
    },
    test("Correctly (in)validate identifier format '\\tfoo'") {
      assert(Cusip.validateFormat("\tfoo"))(isLeft(equalTo(CusipError.InvalidCusipFormat("\tfoo"))))
      assert(Cusip.isValidFormat("\tfoo"))(isFalse)
    },
    test("Correctly (in)validate identifier format 'foo' [STRICT]") {
      assert(Cusip.validateFormatStrict("foo"))(isLeft(equalTo(CusipError.InvalidCusipFormat("foo"))))
      assert(Cusip.isValidFormat("foo"))(isFalse)
    },
    test("Correctly (in)validate payload format '\\tfoo'") {
      assert(Cusip.validatePayloadFormat("\tfoo"))(isLeft(equalTo(CusipError.InvalidPayloadFormat("\tfoo"))))
      assert(Cusip.isValidPayloadFormat("\tfoo"))(isFalse)
    },
    test("Correctly (in)validate payload format 'foo' [STRICT]") {
      assert(Cusip.validatePayloadFormatStrict("foo"))(isLeft(equalTo(CusipError.InvalidPayloadFormat("foo"))))
      assert(Cusip.isValidPayloadFormatStrict("foo"))(isFalse)
    },
    test("Correctly (in)validate Issuer format '\\tfoo'") {
      assert(Cusip.validateIssuerFormat("\tfoo"))(isLeft(equalTo(CusipError.InvalidIssuerFormat("\tfoo"))))
      assert(Cusip.isValidIssuerFormat("\tfoo"))(isFalse)
    },
    test("Correctly (in)validate Issuer format 'foo' [STRICT]") {
      assert(Cusip.validateIssuerFormatStrict("foo"))(isLeft(equalTo(CusipError.InvalidIssuerFormat("foo"))))
      assert(Cusip.isValidIssuerFormatStrict("foo"))(isFalse)
    },
    test("Correctly (in)validate Issue format '\\tfoo'") {
      assert(Cusip.validateIssueFormat("\tfoo"))(isLeft(equalTo(CusipError.InvalidIssueFormat("\tfoo"))))
      assert(Cusip.isValidIssueFormat("\tfoo"))(isFalse)
    },
    test("Correctly (in)validate Issue format 'foo' [STRICT]") {
      assert(Cusip.validateIssueFormatStrict("foo"))(isLeft(equalTo(CusipError.InvalidIssueFormat("foo"))))
      assert(Cusip.isValidIssueFormatStrict("foo"))(isFalse)
    },
    test("Correctly (in)validate Check Digit format '\\tfoo'") {
      assert(Cusip.validateCheckDigitFormat("\tfoo"))(isLeft(equalTo(CusipError.InvalidCheckDigitFormat("\tfoo"))))
      assert(Cusip.isValidCheckDigitFormat("\tfoo"))(isFalse)
    },
    test("Correctly (in)validate Check Digit format 'foo' [STRICT]") {
      assert(Cusip.validateCheckDigitFormatStrict("foo"))(isLeft(equalTo(CusipError.InvalidCheckDigitFormat("foo"))))
      assert(Cusip.isValidCheckDigitFormatStrict("foo"))(isFalse)
    },
    test("Correctly parse the example AAPL CUSIP") {
      val cusip = Cusip.fromString(cusipString).toOption.get

      assert(cusip.value)(equalTo(cusipString))
      assert(cusip.issuerNumber)(equalTo(issuerNumber))
      assert(cusip.issueNumber)(equalTo(issueNumber))
      assert(cusip.checkDigit)(equalTo(checkDigit))
      assert(cusip.payload)(equalTo(s"$issuerNumber$issueNumber"))

      assert(cusip.isCins)(isFalse)
      assert(cusip.isCinsBase)(isFalse)
      assert(cusip.isCinsExtended)(isFalse)
      assert(cusip.cinsCountryCode)(isNone)

      assert(cusip.hasPrivateIssuer)(isFalse)
      assert(cusip.hasPrivateIssue)(isFalse)
      assert(cusip.isPrivateUse)(isFalse)

      assert(cusip.toString)(equalTo(cusipString))
      assert(cusip.toStringTagged)(equalTo(s"cusip:$cusipString"))
    },
    test("Correctly parse the example AAPL CUSIP [STRICT]") {
      val cusip = Cusip.fromStringStrict(cusipString).toOption.get

      assert(cusip.value)(equalTo(cusipString))
      assert(cusip.issuerNumber)(equalTo(issuerNumber))
      assert(cusip.issueNumber)(equalTo(issueNumber))
      assert(cusip.checkDigit)(equalTo(checkDigit))
      assert(cusip.payload)(equalTo(s"$issuerNumber$issueNumber"))

      assert(cusip.isCins)(isFalse)
      assert(cusip.isCinsBase)(isFalse)
      assert(cusip.isCinsExtended)(isFalse)
      assert(cusip.cinsCountryCode)(isNone)

      assert(cusip.hasPrivateIssuer)(isFalse)
      assert(cusip.hasPrivateIssue)(isFalse)
      assert(cusip.isPrivateUse)(isFalse)

      assert(cusip.toString)(equalTo(cusipString))
      assert(cusip.toStringTagged)(equalTo(s"cusip:$cusipString"))
    },
    test("Correctly compute the check digit for the AAPL CUSIP") {
      val result = Cusip.fromPayloadParts(issuerNumber, issueNumber)
      assert(result)(isRight(anything))
      result.map { case cusip @ Cusip(_) =>
        assert(cusip.value)(equalTo(cusipString))
        assert(cusip.checkDigit)(equalTo(checkDigit))
      }
    },
    test("Correctly compute the check digit for the AAPL CUSIP [STRICT]") {
      val result = Cusip.fromPayloadPartsStrict(issuerNumber, issueNumber)
      assert(result)(isRight(anything))
      result.map { case cusip @ Cusip(_) =>
        assert(cusip.value)(equalTo(cusipString))
        assert(cusip.checkDigit)(equalTo(checkDigit))
      }
    },
    test("Correctly validate the check digit for AAPL from the isin.org web site") {
      val cusip = Cusip.fromParts(issuerNumber, issueNumber, checkDigit).toOption.get

      assert(cusip.value)(equalTo(cusipString))
      assert(cusip.issuerNumber)(equalTo(issuerNumber))
      assert(cusip.issueNumber)(equalTo(issueNumber))
      assert(cusip.checkDigit)(equalTo(checkDigit))
    },
    test("Correctly parse and validate a real-world CUSIP with a '0' check digit (BCC aka Boise Cascade)") {
      assert(Cusip.fromString("09739D100").toOption.get.toString)(equalTo("09739D100"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '1' check digit (ADBE aka Adobe)") {
      assert(Cusip.fromString("00724F101").toOption.get.toString)(equalTo("00724F101"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '2' check digit (AAL aka American Airlines)") {
      assert(Cusip.fromString("02376R102").toOption.get.toString)(equalTo("02376R102"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '3' check digit (ADP aka Automatic Data Processing)") {
      assert(Cusip.fromString("053015103").toOption.get.toString)(equalTo("053015103"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '4' check digit (IMKTA aka Ingles Markets)") {
      assert(Cusip.fromString("457030104").toOption.get.toString)(equalTo("457030104"))
    },
    test(
      "Correctly parse and validate a real-world CUSIP with a '5' check digit (AJRD aka Aerojet Rocketdyne Holdings)"
    ) {
      assert(Cusip.fromString("007800105").toOption.get.toString)(equalTo("007800105"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '6' check digit (XRX aka Xerox)") {
      assert(Cusip.fromString("98421M106").toOption.get.toString)(equalTo("98421M106"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '7' check digit (AMD aka Advanced Micro Devices)") {
      assert(Cusip.fromString("007903107").toOption.get.toString)(equalTo("007903107"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '8' check digit (VNDA aka Vanda Pharmaceuticals)") {
      assert(Cusip.fromString("921659108").toOption.get.toString)(equalTo("921659108"))
    },
    test("Correctly parse and validate a real-world CUSIP with a '9' check digit (APT aka AlphaProTec)") {
      assert(Cusip.fromString("020772109").toOption.get.toString)(equalTo("020772109"))
    },
    test("Correctly determine if a CUSIP is a CINS 'base' identifier") {
      val result = for {
        c0 <- Cusip.fromPayload("00000000")
        c9 <- Cusip.fromPayload("99999999")
        cA <- Cusip.fromPayload("AAAAAAAA")
        cH <- Cusip.fromPayload("HHHHHHHH")
        cI <- Cusip.fromPayload("IIIIIIII")
        cJ <- Cusip.fromPayload("JJJJJJJJ")
        cN <- Cusip.fromPayload("NNNNNNNN")
        cO <- Cusip.fromPayload("OOOOOOOO")
        cP <- Cusip.fromPayload("PPPPPPPP")
        cY <- Cusip.fromPayload("YYYYYYYY")
        cZ <- Cusip.fromPayload("ZZZZZZZZ")
      } yield (c0, c9, cA, cH, cI, cJ, cN, cO, cP, cY, cZ)

      assert(result)(isRight(anything))

      val (c0, c9, cA, cH, cI, cJ, cN, cO, cP, cY, cZ) = result.toOption.get

      assert(c0.isCinsBase)(isFalse)
      assert(c9.isCinsBase)(isFalse)
      assert(cA.isCinsBase)(isTrue)
      assert(cH.isCinsBase)(isTrue)
      assert(cI.isCinsBase)(isFalse)
      assert(cJ.isCinsBase)(isTrue)
      assert(cN.isCinsBase)(isTrue)
      assert(cO.isCinsBase)(isFalse)
      assert(cP.isCinsBase)(isTrue)
      assert(cY.isCinsBase)(isTrue)
      assert(cZ.isCinsBase)(isFalse)
    },
    test("Correctly determine if a CUSIP is a CINS 'extended' identifier") {
      val result = for {
        c0 <- Cusip.fromPayload("00000000")
        c9 <- Cusip.fromPayload("99999999")
        cA <- Cusip.fromPayload("AAAAAAAA")
        cH <- Cusip.fromPayload("HHHHHHHH")
        cI <- Cusip.fromPayload("IIIIIIII")
        cJ <- Cusip.fromPayload("JJJJJJJJ")
        cN <- Cusip.fromPayload("NNNNNNNN")
        cO <- Cusip.fromPayload("OOOOOOOO")
        cP <- Cusip.fromPayload("PPPPPPPP")
        cY <- Cusip.fromPayload("YYYYYYYY")
        cZ <- Cusip.fromPayload("ZZZZZZZZ")
      } yield (c0, c9, cA, cH, cI, cJ, cN, cO, cP, cY, cZ)

      assert(result)(isRight(anything))

      val (c0, c9, cA, cH, cI, cJ, cN, cO, cP, cY, cZ) = result.toOption.get

      assert(c0.isCinsExtended)(isFalse)
      assert(c9.isCinsExtended)(isFalse)
      assert(cA.isCinsExtended)(isFalse)
      assert(cH.isCinsExtended)(isFalse)
      assert(cI.isCinsExtended)(isTrue)
      assert(cJ.isCinsExtended)(isFalse)
      assert(cN.isCinsExtended)(isFalse)
      assert(cO.isCinsExtended)(isTrue)
      assert(cP.isCinsExtended)(isFalse)
      assert(cY.isCinsExtended)(isFalse)
      assert(cZ.isCinsExtended)(isTrue)
    },
    test("Correctly determine if a CUSIP is a CINS 'extended' identifier") {
      val result = for {
        c0 <- Cusip.fromPayload("00000000")
        c9 <- Cusip.fromPayload("99999999")
        cA <- Cusip.fromPayload("AAAAAAAA")
        cZ <- Cusip.fromPayload("ZZZZZZZZ")
      } yield (c0, c9, cA, cZ)

      assert(result)(isRight(anything))

      val (c0, c9, cA, cZ) = result.toOption.get

      assert(c0.isCins)(isFalse)
      assert(c9.isCins)(isFalse)
      assert(cA.isCins)(isTrue)
      assert(cZ.isCins)(isTrue)
    },
    test("Correctly support default Ordering") {
      val xrx = Cusip.fromString("98421M106").toOption.get
      val amd = Cusip.fromString("007903107").toOption.get
      val chunk = Chunk(xrx, amd)
      val sorted = chunk.sorted
      assert(sorted)(equalTo(Chunk(amd, xrx)))
    },
    test("Correctly support pattern matching") {
      val xrx = Cusip.fromString("98421M106").toOption.get
      val amd = Cusip.fromString("007903107").toOption.get
      val chunk = Chunk(xrx, amd).map { case Cusip(v) => v }
      assert(chunk)(equalTo(Chunk("98421M106", "007903107")))
    }
  )

}
