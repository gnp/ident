/*
 * Copyright 2023-2025 Gregor Purdy
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

class FigiSpec extends AnyFunSpec with should.Matchers {

  describe("FigiSpec") {
    it("Parse and validate the FIGI for ticker TNK (TeeKay Tankers)") {
      val figiString = "BBG000QRMZH1"
      val provider = "BB"
      val scope = "G"
      val id = "000QRMZH"
      val checkDigit = "1"

      val figi = Figi.fromString(figiString).toOption.get

      figi.value shouldBe figiString
      figi.provider shouldBe provider
      figi.scope shouldBe scope
      figi.id shouldBe id
      figi.checkDigit shouldBe checkDigit
    }

    it("Parse and validate FIGIs from section 6.1.2, page 17 of the OMG spec") {
      Figi.fromString("BBG000BLNQ16") shouldBe a[Right[?, ?]]
      Figi.fromString("NRG92C84SB39") shouldBe a[Right[?, ?]]
    }

    it("Parse and validate FIGIs from other offical documents with various check digits") {
      // YHO GR composite level FIGI
      Figi.fromString("BBG000MG1P20") shouldBe a[Right[?, ?]]
      // Yahoo share class level FIGI
      Figi.fromString("BBG001S8V781") shouldBe a[Right[?, ?]]
      // YHOO TE composite level FIGI
      Figi.fromString("BBG000SF0LF2") shouldBe a[Right[?, ?]]
      // YHOG IX composite level FIGI
      Figi.fromString("BBG000Q8SXG3") shouldBe a[Right[?, ?]]
      // YHOO PE composite level FIGI
      Figi.fromString("BBG000KB2D74") shouldBe a[Right[?, ?]]
      // YHO TH composite level FIGI
      Figi.fromString("BBG000TL3H46") shouldBe a[Right[?, ?]]
      // YHOO US composite level FIGI
      Figi.fromString("BBG000GFFQN9") shouldBe a[Right[?, ?]]
      // ADRs of Vale SA share class level FIGI
      Figi.fromString("BBG001SB54S8") shouldBe a[Right[?, ?]]
      // IBM-U2 OC futures
      Figi.fromString("BBG002F43VC5") shouldBe a[Right[?, ?]]
      // IBM=4 OC futures
      Figi.fromString("BBG000S52WG7") shouldBe a[Right[?, ?]]
    }

    it("Correctly support default Ordering") {
      val a = Figi.fromString("NRG92C84SB39").toOption.get
      val b = Figi.fromString("BBG000BLNQ16").toOption.get
      val seq = Seq(a, b)
      val sorted = seq.sorted
      sorted shouldBe Seq(b, a)
    }

    it("Correctly support pattern matching") {
      val a = Figi.fromString("NRG92C84SB39").toOption.get
      val b = Figi.fromString("BBG000BLNQ16").toOption.get
      val seq = Seq(a, b).map { case Figi(v) => v }
      seq shouldBe Seq("NRG92C84SB39", "BBG000BLNQ16")
    }
  }
}
