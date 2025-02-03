/*
 * Copyright 2023 Gregor Purdy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gregorpurdy.ident

import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*

object IsinSpec extends AnyFunSpec with should.Matchers {

  val isinString = "US0378331005"
  val countryCode = "US"
  val securityIdentifier = "037833100"
  val checkDigit = "5"

  describe("Isin") {
    it("should correctly parse and validate the example AAPL ISIN from the isin.org web site") {
      val isin = Isin.fromString(isinString).toOption.get

      isin.value shouldBe isinString
      isin.countryCode shouldBe countryCode
      isin.securityIdentifier shouldBe securityIdentifier
      isin.checkDigit shouldBe checkDigit
    }

    it("should correctly compute the check digit for AAPL from the isin.org web site") {
      val result = Isin.fromPayloadParts(countryCode, securityIdentifier)
      result shouldBe a[Right[?, ?]]

      val isin = result.toOption.get
      isin.value shouldBe isinString
      isin.countryCode shouldBe countryCode
      isin.securityIdentifier shouldBe securityIdentifier
      isin.checkDigit shouldBe checkDigit
    }

    it("should correctly validate the check digit for AAPL from the isin.org web site") {
      val isin = Isin.fromParts(countryCode, securityIdentifier, checkDigit).toOption.get

      isin.value shouldBe isinString
      isin.countryCode shouldBe countryCode
      isin.securityIdentifier shouldBe securityIdentifier
      isin.checkDigit shouldBe checkDigit
    }

    it("should correctly parse and validate a real-world ISIN with a '0' check digit (BCC aka Boise Cascade)") {
      Isin.fromString("US09739D1000").toOption.get.toString shouldBe "US09739D1000"
    }

    it("should correctly parse and validate a real-world ISIN with a '1' check digit (INTC aka Intel)") {
      Isin.fromString("US4581401001").toOption.get.toString shouldBe "US4581401001"
    }

    it("should correctly parse and validate a real-world ISIN with a '2' check digit (XRX aka Xerox)") {
      Isin.fromString("US98421M1062").toOption.get.toString shouldBe "US98421M1062"
    }

    it("should correctly parse and validate a real-world ISIN with a '3' check digit (AAL aka American Airlines)") {
      Isin.fromString("US02376R1023").toOption.get.toString shouldBe "US02376R1023"
    }

    it(
      "should correctly parse and validate a real-world ISIN with a '4' check digit (VNDA aka Vanda Pharmaceuticals)"
    ) {
      Isin.fromString("US9216591084").toOption.get.toString shouldBe "US9216591084"
    }

    it("should correctly parse and validate a real-world ISIN with a '5' check digit (APT aka AlphaProTec)") {
      Isin.fromString("US0207721095").toOption.get.toString shouldBe "US0207721095"
    }

    it("should correctly parse and validate a real-world ISIN with a '6' check digit (PRDO aka Perdoceo Education)") {
      Isin.fromString("US71363P1066").toOption.get.toString shouldBe "US71363P1066"
    }

    it("should correctly parse and validate a real-world ISIN with a '7' check digit (MEI aka Methode Electronics)") {
      Isin.fromString("US5915202007").toOption.get.toString shouldBe "US5915202007"
    }

    it("should correctly parse and validate a real-world ISIN with a '8' check digit (IMKTA aka Ingles Markets)") {
      Isin.fromString("US4570301048").toOption.get.toString shouldBe "US4570301048"
    }

    it(
      "should correctly parse and validate a real-world ISIN with a '9' check digit (SUPN aka Supernus Pharmaceuticals)"
    ) {
      Isin.fromString("US8684591089").toOption.get.toString shouldBe "US8684591089"
    }

    it("should correctly support default Ordering") {
      val bcc = Isin.fromString("US09739D1000").toOption.get
      val intc = Isin.fromString("US4581401001").toOption.get
      val seq = Seq(intc, bcc)
      val sorted = seq.sorted
      sorted shouldBe Seq(bcc, intc)
    }

    it("should correctly support pattern matching") {
      val bcc = Isin.fromString("US09739D1000").toOption.get
      val intc = Isin.fromString("US4581401001").toOption.get
      val seq = Seq(intc, bcc).map { case Isin(v) => v }
      seq shouldBe Seq("US4581401001", "US09739D1000")
    }
  }

}
