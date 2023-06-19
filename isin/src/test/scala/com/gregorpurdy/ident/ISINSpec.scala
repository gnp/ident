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

import org.scalatest.flatspec._

class ISINSpec extends AnyFlatSpec {

  val isinString = "US0378331005"
  val countryCode = "US"
  val securityIdentifier = "037833100"
  val checkDigit = "5"

  it should "Correctly parse and validate the example AAPL ISIN from the isin.org web site" in {
    val isin = ISIN(isinString)

    assert(isin.value === isinString)
    assert(isin.countryCode === countryCode)
    assert(isin.securityIdentifier === securityIdentifier)
    assert(isin.checkDigit === checkDigit)
  }

  it should "Correctly compute the check digit for AAPL from the isin.org web site" in {
    val isin = ISIN(countryCode, securityIdentifier)

    assert(isin.value === isinString)
    assert(isin.countryCode === countryCode)
    assert(isin.securityIdentifier === securityIdentifier)
    assert(isin.checkDigit === checkDigit)
  }

  it should "Correctly validate the check digit for AAPL from the isin.org web site" in {
    val isin = ISIN(countryCode, securityIdentifier, checkDigit)

    assert(isin.value === isinString)
    assert(isin.countryCode === countryCode)
    assert(isin.securityIdentifier === securityIdentifier)
    assert(isin.checkDigit === checkDigit)
  }

  it should "Correctly parse and validate a real-world ISIN with a '0' check digit (BCC aka Boise Cascade)" in {
    ISIN("US09739D1000")
  }

  it should "Correctly parse and validate a real-world ISIN with a '1' check digit (INTC aka Intel)" in {
    ISIN("US4581401001")
  }

  it should "Correctly parse and validate a real-world ISIN with a '2' check digit (XRX aka Xerox)" in {
    ISIN("US98421M1062")
  }

  it should "Correctly parse and validate a real-world ISIN with a '3' check digit (AAL aka American Airlines)" in {
    ISIN("US02376R1023")
  }

  it should "Correctly parse and validate a real-world ISIN with a '4' check digit (VNDA aka Vanda Pharmaceuticals)" in {
    ISIN("US9216591084")
  }

  it should "Correctly parse and validate a real-world ISIN with a '5' check digit (APT aka AlphaProTec)" in {
    ISIN("US0207721095")
  }

  it should "Correctly parse and validate a real-world ISIN with a '6' check digit (PRDO aka Perdoceo Education)" in {
    ISIN("US71363P1066")
  }

  it should "Correctly parse and validate a real-world ISIN with a '7' check digit (MEI aka Methode Electronics)" in {
    ISIN("US5915202007")
  }

  it should "Correctly parse and validate a real-world ISIN with a '8' check digit (IMKTA aka Ingles Markets)" in {
    ISIN("US4570301048")
  }

  it should "Correctly parse and validate a real-world ISIN with a '9' check digit (SUPN aka Supernus Pharmaceuticals)" in {
    ISIN("US8684591089")
  }

}
