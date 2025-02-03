package com.gregorpurdy.ident

import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*

class CusipSpec extends AnyFunSpec with should.Matchers {

  val issuerNumber = "037833"
  val issueNumber = "10"
  val checkDigit = "0"
  val payload = s"$issuerNumber$issueNumber"
  val cusipString = s"$payload$checkDigit"

  describe("Cusip") {
    it("should correctly validate the AAPL CUSIP Issuer number") {
      Cusip.validateIssuerFormat(issuerNumber) shouldBe Right(issuerNumber)
    }

    it("should correctly validate the AAPL CUSIP Issue number") {
      Cusip.validateIssueFormat(issueNumber) shouldBe Right(issueNumber)
    }

    it("should correctly validate the AAPL CUSIP payload") {
      Cusip.validatePayloadFormat(payload) shouldBe Right(payload)
    }

    it("should correctly validate the AAPL CUSIP Check Digit") {
      Cusip.validateCheckDigitFormat(checkDigit) shouldBe Right(checkDigit)
    }

    it("should correctly return an error when validating Check Digit '1' for Issuer '111111' and Issue '11'") {
      val expected = CusipError.IncorrectCheckDigitValue("1", "8", "111111", "11")
      Cusip.validateCheckDigitForPartsInternal("111111", "11", "1") shouldBe Left(expected)
    }

    it("should correctly return an error when parsing '111111111'") {
      val expected = CusipError.IncorrectCheckDigitValue("1", "8", "111111", "11")
      Cusip.fromString("111111111") shouldBe Left(expected)
    }

    it("should correctly validate the example AAPL CUSIP") {
      Cusip.validateFormat("\t" + cusipString) shouldBe Right(cusipString)
      Cusip.isValidFormat("\t" + cusipString) shouldBe true
    }

    it("should correctly validate the example AAPL CUSIP [STRICT]") {
      Cusip.validateFormatStrict(cusipString) shouldBe Right(cusipString)
      Cusip.isValidFormatStrict(cusipString) shouldBe true
    }

    it("should correctly (in)validate identifier format '\\tfoo'") {
      Cusip.validateFormat("\tfoo") shouldBe Left(CusipError.InvalidCusipFormat("\tfoo"))
      Cusip.isValidFormat("\tfoo") shouldBe false
    }

    it("should correctly (in)validate identifier format 'foo' [STRICT]") {
      Cusip.validateFormatStrict("foo") shouldBe Left(CusipError.InvalidCusipFormat("foo"))
      Cusip.isValidFormatStrict("foo") shouldBe false
    }

    it("should correctly (in)validate payload format '\\tfoo'") {
      Cusip.validatePayloadFormat("\tfoo") shouldBe Left(CusipError.InvalidPayloadFormat("\tfoo"))
      Cusip.isValidPayloadFormat("\tfoo") shouldBe false
    }

    it("should correctly (in)validate payload format 'foo' [STRICT]") {
      Cusip.validatePayloadFormatStrict("foo") shouldBe Left(CusipError.InvalidPayloadFormat("foo"))
      Cusip.isValidPayloadFormatStrict("foo") shouldBe false
    }

    it("should correctly (in)validate Issuer format '\\tfoo'") {
      Cusip.validateIssuerFormat("\tfoo") shouldBe Left(CusipError.InvalidIssuerFormat("\tfoo"))
      Cusip.isValidIssuerFormat("\tfoo") shouldBe false
    }

    it("should correctly (in)validate Issuer format 'foo' [STRICT]") {
      Cusip.validateIssuerFormatStrict("foo") shouldBe Left(CusipError.InvalidIssuerFormat("foo"))
      Cusip.isValidIssuerFormatStrict("foo") shouldBe false
    }

    it("should correctly (in)validate Issue format '\\tfoo'") {
      Cusip.validateIssueFormat("\tfoo") shouldBe Left(CusipError.InvalidIssueFormat("\tfoo"))
      Cusip.isValidIssueFormat("\tfoo") shouldBe false
    }

    it("should correctly (in)validate Issue format 'foo' [STRICT]") {
      Cusip.validateIssueFormatStrict("foo") shouldBe Left(CusipError.InvalidIssueFormat("foo"))
      Cusip.isValidIssueFormatStrict("foo") shouldBe false
    }

    it("should correctly (in)validate Check Digit format '\\tfoo'") {
      Cusip.validateCheckDigitFormat("\tfoo") shouldBe Left(CusipError.InvalidCheckDigitFormat("\tfoo"))
      Cusip.isValidCheckDigitFormat("\tfoo") shouldBe false
    }

    it("should correctly (in)validate Check Digit format 'foo' [STRICT]") {
      Cusip.validateCheckDigitFormatStrict("foo") shouldBe Left(CusipError.InvalidCheckDigitFormat("foo"))
      Cusip.isValidCheckDigitFormatStrict("foo") shouldBe false
    }

    it("should correctly parse the example AAPL CUSIP") {
      val cusip = Cusip.fromString(cusipString).toOption.get

      cusip.value shouldBe cusipString
      cusip.issuerNumber shouldBe issuerNumber
      cusip.issueNumber shouldBe issueNumber
      cusip.checkDigit shouldBe checkDigit
      cusip.payload shouldBe s"$issuerNumber$issueNumber"

      cusip.isCins shouldBe false
      cusip.isCinsBase shouldBe false
      cusip.isCinsExtended shouldBe false
      cusip.cinsCountryCode shouldBe None

      cusip.hasPrivateIssuer shouldBe false
      cusip.hasPrivateIssue shouldBe false
      cusip.isPrivateUse shouldBe false

      cusip.toString shouldBe cusipString
      cusip.toStringTagged shouldBe s"cusip:$cusipString"
    }

    it("should correctly parse the example AAPL CUSIP [STRICT]") {
      val cusip = Cusip.fromStringStrict(cusipString).toOption.get

      cusip.value shouldBe cusipString
      cusip.issuerNumber shouldBe issuerNumber
      cusip.issueNumber shouldBe issueNumber
      cusip.checkDigit shouldBe checkDigit
      cusip.payload shouldBe s"$issuerNumber$issueNumber"

      cusip.isCins shouldBe false
      cusip.isCinsBase shouldBe false
      cusip.isCinsExtended shouldBe false
      cusip.cinsCountryCode shouldBe None

      cusip.hasPrivateIssuer shouldBe false
      cusip.hasPrivateIssue shouldBe false
      cusip.isPrivateUse shouldBe false

      cusip.toString shouldBe cusipString
      cusip.toStringTagged shouldBe s"cusip:$cusipString"
    }

    it("should correctly compute the check digit for the AAPL CUSIP") {
      val cusip = Cusip.fromPayloadParts(issuerNumber, issueNumber).toOption.get

      cusip.value shouldBe cusipString
      cusip.checkDigit shouldBe checkDigit
    }

    it("should correctly compute the check digit for the AAPL CUSIP [STRICT]") {
      val cusip = Cusip.fromPayloadPartsStrict(issuerNumber, issueNumber).toOption.get

      cusip.value shouldBe cusipString
      cusip.checkDigit shouldBe checkDigit
    }

    it("should correctly validate the check digit for AAPL from the isin.org web site") {
      val cusip = Cusip.fromParts(issuerNumber, issueNumber, checkDigit).toOption.get

      cusip.value shouldBe cusipString
      cusip.issuerNumber shouldBe issuerNumber
      cusip.issueNumber shouldBe issueNumber
      cusip.checkDigit shouldBe checkDigit
    }

    it("should correctly parse and validate a real-world CUSIP with a '0' check digit (BCC aka Boise Cascade)") {
      Cusip.fromString("09739D100").toOption.get.toString shouldBe "09739D100"
    }

    it("should correctly parse and validate a real-world CUSIP with a '1' check digit (ADBE aka Adobe)") {
      Cusip.fromString("00724F101").toOption.get.toString shouldBe "00724F101"
    }

    it("should correctly parse and validate a real-world CUSIP with a '2' check digit (AAL aka American Airlines)") {
      Cusip.fromString("02376R102").toOption.get.toString shouldBe "02376R102"
    }

    it(
      "should correctly parse and validate a real-world CUSIP with a '3' check digit (ADP aka Automatic Data Processing)"
    ) {
      Cusip.fromString("053015103").toOption.get.toString shouldBe "053015103"
    }

    it("should correctly parse and validate a real-world CUSIP with a '4' check digit (IMKTA aka Ingles Markets)") {
      Cusip.fromString("457030104").toOption.get.toString shouldBe "457030104"
    }

    it(
      "should correctly parse and validate a real-world CUSIP with a '5' check digit (AJRD aka Aerojet Rocketdyne Holdings)"
    ) {
      Cusip.fromString("007800105").toOption.get.toString shouldBe "007800105"
    }

    it("should correctly parse and validate a real-world CUSIP with a '6' check digit (XRX aka Xerox)") {
      Cusip.fromString("98421M106").toOption.get.toString shouldBe "98421M106"
    }

    it(
      "should correctly parse and validate a real-world CUSIP with a '7' check digit (AMD aka Advanced Micro Devices)"
    ) {
      Cusip.fromString("007903107").toOption.get.toString shouldBe "007903107"
    }

    it(
      "should correctly parse and validate a real-world CUSIP with a '8' check digit (VNDA aka Vanda Pharmaceuticals)"
    ) {
      Cusip.fromString("921659108").toOption.get.toString shouldBe "921659108"
    }

    it("should correctly parse and validate a real-world CUSIP with a '9' check digit (APT aka AlphaProTec)") {
      Cusip.fromString("020772109").toOption.get.toString shouldBe "020772109"
    }

    it("should correctly determine if a CUSIP is a CINS 'base' identifier") {
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

      result shouldBe a[Right[?, ?]]

      val (c0, c9, cA, cH, cI, cJ, cN, cO, cP, cY, cZ) = result.toOption.get

      c0.isCinsBase shouldBe false
      c9.isCinsBase shouldBe false
      cA.isCinsBase shouldBe true
      cH.isCinsBase shouldBe true
      cI.isCinsBase shouldBe false
      cJ.isCinsBase shouldBe true
      cN.isCinsBase shouldBe true
      cO.isCinsBase shouldBe false
      cP.isCinsBase shouldBe true
      cY.isCinsBase shouldBe true
      cZ.isCinsBase shouldBe false
    }

    it("should correctly determine if a CUSIP is a CINS 'extended' identifier") {
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

      result shouldBe a[Right[?, ?]]

      val (c0, c9, cA, cH, cI, cJ, cN, cO, cP, cY, cZ) = result.toOption.get

      c0.isCinsExtended shouldBe false
      c9.isCinsExtended shouldBe false
      cA.isCinsExtended shouldBe false
      cH.isCinsExtended shouldBe false
      cI.isCinsExtended shouldBe true
      cJ.isCinsExtended shouldBe false
      cN.isCinsExtended shouldBe false
      cO.isCinsExtended shouldBe true
      cP.isCinsExtended shouldBe false
      cY.isCinsExtended shouldBe false
      cZ.isCinsExtended shouldBe true
    }

    it("should correctly determine if a CUSIP is a CINS identifier") {
      val result = for {
        c0 <- Cusip.fromPayload("00000000")
        c9 <- Cusip.fromPayload("99999999")
        cA <- Cusip.fromPayload("AAAAAAAA")
        cZ <- Cusip.fromPayload("ZZZZZZZZ")
      } yield (c0, c9, cA, cZ)

      result shouldBe a[Right[?, ?]]

      val (c0, c9, cA, cZ) = result.toOption.get

      c0.isCins shouldBe false
      c9.isCins shouldBe false
      cA.isCins shouldBe true
      cZ.isCins shouldBe true
    }

    it("should parse a collection of example CUSIPs from SEC data") {
      // Test data source: https://www.sec.gov/divisions/investment/13flists.htm
      val cases = List(
        "25470F104",
        "254709108",
        "254709108",
        "25470F104",
        "25470F302",
        "25470M109",
        "25490H106",
        "25490K273",
        "25490K281",
        "25490K323",
        "25490K331",
        "25490K596",
        "25490K869",
        "25525P107",
        "255519100",
        "256135203",
        "25614T309",
        "256163106",
        "25659T107",
        "256677105",
        "256746108",
        "25746U109",
        "25754A201",
        "257554105",
        "257559203",
        "257651109",
        "257701201",
        "257867200",
        "25787G100",
        "25809K105",
        "25820R105",
        "258278100",
        "258622109",
        "25960P109",
        "25960R105",
        "25985W105",
        "260003108",
        "260174107",
        "260557103",
        "26140E600",
        "26142R104",
        "26152H301",
        "262037104",
        "262077100",
        "26210C104",
        "264120106",
        "264147109",
        "264411505",
        "26441C204",
        "26443V101",
        "26484T106",
        "265504100",
        "26614N102",
        "266605104",
        "26745T101",
        "267475101",
        "268150109",
        "268158201",
        "26817Q886",
        "268311107",
        "26856L103",
        "268603107",
        "26874R108",
        "26884L109",
        "26884U109",
        "268948106",
        "26922A230",
        "26922A248",
        "26922A289",
        "26922A305"
      )

      val results = cases.map(s => (s -> Cusip.fromString(s).toOption.get))
      results.length shouldBe cases.length
    }

    it("should correctly support default Ordering") {
      val xrx = Cusip.fromString("98421M106").toOption.get
      val amd = Cusip.fromString("007903107").toOption.get
      val seq = Seq(xrx, amd)
      val sorted = seq.sorted
      sorted shouldBe Seq(amd, xrx)
    }

    it("should correctly support pattern matching") {
      val xrx = Cusip.fromString("98421M106").toOption.get
      val amd = Cusip.fromString("007903107").toOption.get
      val seq = Seq(xrx, amd).map { case Cusip(v) => v }
      seq shouldBe Seq("98421M106", "007903107")
    }
  }

}
