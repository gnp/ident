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

object LeiSpec extends ZIOSpecDefault {

  /** These are from the ISIN_LEI_20210209.csv file from GLEIF.
    */
  val conformingCases = List(
    "635400B4JJBON4TCHF02",
    "529900ODI3047E2LIV03",
    "5493002F3N6V3Z14SP04",
    "549300IYKILIU506KA05",
    "JJKC32MCHWDI71265Z06",
    "549300RIPPWJB5Z0FK07",
    "Z2VZBHUMB7PWWJ63I008",
    "FRQ78DFDYWMT3XY6UR09",
    "337KMNHEWWWR6B7Q7W10",
    "549300E9PC51EN656011",
    "5493003WHB7TFLYQFS12",
    "549300C04BJ0G297NC13",
    "T68X8LLAQYRNDV034K14",
    "8HWWA59ZS6Z54QLX6S15",
    "54930018SOOHBHRLWC16",
    "95980020140005346817",
    "549300HMMEWVG3PPQU18",
    "5JQ7W3GWO8J5DAE5WR19",
    "AJ6VL0Z1WDC42KKJZO20"
  )

  def conformingCaseTest(value: String): Spec[Any, Nothing] = suite(s"Conforming LEI '$value'")(
    test("Correctly parse") {
      assert(Lei.fromString(value))(isRight(hasField("isConforming", _.isConforming, isTrue)))
    },
    test("Correctly validate") {
      assertTrue(Lei.validate(value))
    },
    test("Correctly validateAllowNonConforming(_)") {
      assertTrue(Lei.validateAllowNonConforming(value))
    }
  )

  def conformingCasesSuite =
    suite("Parse and validate conforming LEIs from ISIN_LEI_20210209.csv")(conformingCases.map(conformingCaseTest): _*)

  /** These come from the ISIN_LEI_20210209.csv file. Note that according to the ISO standard itself, section 5 "Check
    * digit pair", subsection 5.1 "General": "00, 01 and 99 are not valid LEI check digit pairs".
    */
  val nonConformingCases = List(
    "31570010000000045200",
    "3157006B6JVZ5DFMSN00",
    "315700BBRQHDWX6SHZ00",
    "315700G5G24XYL1TXH00",
    "31570010000000048401",
    "31570010000000067801",
    "315700WH3YMKHCVYW201"
  )

  def nonConformingCaseTest(value: String): Spec[Any, Nothing] = suite(s"Non-conforming LEI '$value'")(
    test("Correctly parse") {
      assert(Lei.fromString(value))(isRight(hasField("isConforming", _.isConforming, isFalse)))
    },
    test("Correctly fail to validate(_)") {
      assertTrue(!Lei.validate(value))
    },
    test("Correctly validateAllowNonConforming(_)") {
      assertTrue(Lei.validateAllowNonConforming(value))
    },
    /*
     * NOTE: These identifiers from the GLEIF data file are NOT valid because they end in "00" or "01" which subsection
     * 5.1 of the ISO standard explicitly disallow along with "99". However, if you only run the check-digit-validation
     * step without rejecting them a priori, then they will appear to be valid LEIs (if the compute_iso7064_mod97_10
     * method returns 1 for a 20-digit LEI then it is possibly valid (although you are also supposed to check that the
     * check digits are in the range [02-98] also. This test is here to demonstrate that the bad LEIs in the GLEIF data
     * are probably there because the range check was not done to further validate purported LEIs that passed the
     * "MOD 97-10" check alone.
     */
    test(
      s"Correctly get 1 for compute_iso7064_mod97_10(_)"
    ) {
      val result = Lei.compute_iso7064_mod97_10(value)
      assert(result)(equalTo(1))
    }
  )

  def nonConformingCasesSuite =
    suite("Parse and validate non-conforming LEIs with *00 and *01 formats from ISIN_LEI_20210209.csv")(
      nonConformingCases.map(nonConformingCaseTest): _*
    )

  def spec: Spec[Any, Any] = suite("LeiSpec")(
    test(s"Correctly parse and validate LEI from section A.1 example calculation in the ISO 17442-1:2020(E) spec") {
      val lou = "YZ83"
      val entity = "GD8L7GG84979J5"

      val lei = Lei.fromPartsCalcCheckDigits(lou, entity).toOption.get
      assert(lei.louIdentifier)(equalTo(lou))
      assert(lei.entityIdentifier)(equalTo(entity))

      val checkDigits = "16"
      assert(lei.checkDigits)(equalTo(checkDigits))

      val leiString = s"${lou}${entity}${checkDigits}"
      assert(lei.value)(equalTo(leiString))
    },
    conformingCasesSuite,
    nonConformingCasesSuite,
    test("Correctly support default Ordering") {
      val a = Lei.fromString("635400B4JJBON4TCHF02").toOption.get
      val b = Lei.fromString("529900ODI3047E2LIV03").toOption.get
      val chunk = Chunk(a, b)
      val sorted = chunk.sorted
      assert(sorted)(equalTo(Chunk(b, a)))
    },
    test("Correctly support pattern matching") {
      val a = Lei.fromString("635400B4JJBON4TCHF02").toOption.get
      val b = Lei.fromString("529900ODI3047E2LIV03").toOption.get
      val chunk = Chunk(a, b).map { case Lei(v) => v }
      assert(chunk)(equalTo(Chunk("635400B4JJBON4TCHF02", "529900ODI3047E2LIV03")))
    }
  )
}
