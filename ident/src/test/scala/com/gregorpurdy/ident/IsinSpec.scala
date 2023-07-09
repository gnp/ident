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

import zio.test.Assertion.*
import zio.test.*

object IsinSpec extends ZIOSpecDefault {

  val isinString = "US0378331005"
  val countryCode = "US"
  val securityIdentifier = "037833100"
  val checkDigit = "5"

  def spec: Spec[Any, Any] = suite("IsinSpec")(
    test("Correctly parse and validate the example AAPL ISIN from the isin.org web site") {
      val isin = Isin.fromString(isinString).toOption.get

      assert(isin.value)(equalTo(isinString))
      assert(isin.countryCode)(equalTo(countryCode))
      assert(isin.securityIdentifier)(equalTo(securityIdentifier))
      assert(isin.checkDigit)(equalTo(checkDigit))
    },
    test("Correctly compute the check digit for AAPL from the isin.org web site") {
      val isin = Isin.fromPartsCalcCheckDigit(countryCode, securityIdentifier).toOption.get

      assert(isin.value)(equalTo(isinString))
      assert(isin.countryCode)(equalTo(countryCode))
      assert(isin.securityIdentifier)(equalTo(securityIdentifier))
      assert(isin.checkDigit)(equalTo(checkDigit))
    },
    test("Correctly validate the check digit for AAPL from the isin.org web site") {
      val isin = Isin.fromParts(countryCode, securityIdentifier, checkDigit).toOption.get

      assert(isin.value)(equalTo(isinString))
      assert(isin.countryCode)(equalTo(countryCode))
      assert(isin.securityIdentifier)(equalTo(securityIdentifier))
      assert(isin.checkDigit)(equalTo(checkDigit))
    },
    test("Correctly parse and validate a real-world ISIN with a '0' check digit (BCC aka Boise Cascade)") {
      assert(Isin.fromString("US09739D1000").toOption.get.toString)(equalTo("US09739D1000"))
    },
    test("Correctly parse and validate a real-world ISIN with a '1' check digit (INTC aka Intel)") {
      assert(Isin.fromString("US4581401001").toOption.get.toString)(equalTo("US4581401001"))
    },
    test("Correctly parse and validate a real-world ISIN with a '2' check digit (XRX aka Xerox)") {
      assert(Isin.fromString("US98421M1062").toOption.get.toString)(equalTo("US98421M1062"))
    },
    test("Correctly parse and validate a real-world ISIN with a '3' check digit (AAL aka American Airlines)") {
      assert(Isin.fromString("US02376R1023").toOption.get.toString)(equalTo("US02376R1023"))
    },
    test("Correctly parse and validate a real-world ISIN with a '4' check digit (VNDA aka Vanda Pharmaceuticals)") {
      assert(Isin.fromString("US9216591084").toOption.get.toString)(equalTo("US9216591084"))
    },
    test("Correctly parse and validate a real-world ISIN with a '5' check digit (APT aka AlphaProTec)") {
      assert(Isin.fromString("US0207721095").toOption.get.toString)(equalTo("US0207721095"))
    },
    test("Correctly parse and validate a real-world ISIN with a '6' check digit (PRDO aka Perdoceo Education)") {
      assert(Isin.fromString("US71363P1066").toOption.get.toString)(equalTo("US71363P1066"))
    },
    test("Correctly parse and validate a real-world ISIN with a '7' check digit (MEI aka Methode Electronics)") {
      assert(Isin.fromString("US5915202007").toOption.get.toString)(equalTo("US5915202007"))
    },
    test("Correctly parse and validate a real-world ISIN with a '8' check digit (IMKTA aka Ingles Markets)") {
      assert(Isin.fromString("US4570301048").toOption.get.toString)(equalTo("US4570301048"))
    },
    test("Correctly parse and validate a real-world ISIN with a '9' check digit (SUPN aka Supernus Pharmaceuticals)") {
      assert(Isin.fromString("US8684591089").toOption.get.toString)(equalTo("US8684591089"))
    }
  )

}
