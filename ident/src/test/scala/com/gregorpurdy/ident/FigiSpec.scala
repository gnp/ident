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

import zio.Chunk
import zio.test.Assertion.*
import zio.test.*

object FigiSpec extends ZIOSpecDefault {

  def spec: Spec[Any, Any] = suite("FigiSpec")(
    test("Parse and validate the FIGI for ticker TNK (TeeKay Tankers)") {
      val figiString = "BBG000QRMZH1"
      val provider = "BB"
      val scope = "G"
      val id = "000QRMZH"
      val checkDigit = "1"

      val figi = Figi.fromString(figiString).toOption.get

      assert(figi.value)(equalTo(figiString))
      assert(figi.provider)(equalTo(provider))
      assert(figi.scope)(equalTo(scope))
      assert(figi.id)(equalTo(id))
      assert(figi.checkDigit)(equalTo(checkDigit))
    },
    test("Parse and validate FIGIs from section 6.1.2, page 17 of the OMG spec") {
      assert(Figi.fromString("BBG000BLNQ16"))(isRight)
      assert(Figi.fromString("NRG92C84SB39"))(isRight)
    },
    test("Parse and validate FIGIs from other offical documents with various check digits") {
      assert(Figi.fromString("BBG000MG1P20"))(isRight) // YHO GR composite level FIGI
      assert(Figi.fromString("BBG001S8V781"))(isRight) // Yahoo share class level FIGI
      assert(Figi.fromString("BBG000SF0LF2"))(isRight) // YHOO TE composite level FIGI
      assert(Figi.fromString("BBG000Q8SXG3"))(isRight) // YHOG IX composite level FIGI
      assert(Figi.fromString("BBG000KB2D74"))(isRight) // YHOO PE composite level FIGI
      assert(Figi.fromString("BBG000TL3H46"))(isRight) // YHO TH composite level FIGI
      assert(Figi.fromString("BBG000GFFQN9"))(isRight) // YHOO US composite level FIGI

      assert(Figi.fromString("BBG001SB54S8"))(isRight) // ADRs of Vale SA share class level FIGI

      assert(Figi.fromString("BBG002F43VC5"))(isRight) // IBM-U2 OC futures
      assert(Figi.fromString("BBG000S52WG7"))(isRight) // IBM=4 OC futures
    },
    test("Correctly support default Ordering") {
      val a = Figi.fromString("NRG92C84SB39").toOption.get
      val b = Figi.fromString("BBG000BLNQ16").toOption.get
      val chunk = Chunk(a, b)
      val sorted = chunk.sorted
      assert(sorted)(equalTo(Chunk(b, a)))
    },
    test("Correctly support pattern matching") {
      val a = Figi.fromString("NRG92C84SB39").toOption.get
      val b = Figi.fromString("BBG000BLNQ16").toOption.get
      val chunk = Chunk(a, b).map { case Figi(v) => v }
      assert(chunk)(equalTo(Chunk("NRG92C84SB39", "BBG000BLNQ16")))
    }
  )

}
