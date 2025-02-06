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

import com.gregorpurdy.ccs.IsoIec7064
import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*

class LeiSpec extends AnyFunSpec with should.Matchers {

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

  describe("Lei") {
    describe("ISO 17442-1:2020(E) spec example") {
      it("should correctly parse and validate LEI from section A.1") {
        val lou = "YZ83"
        val entity = "GD8L7GG84979J5"

        val lei = Lei.fromPartsCalcCheckDigits(lou, entity).toOption.get
        lei.louIdentifier shouldBe lou
        lei.entityIdentifier shouldBe entity

        val checkDigits = "16"
        lei.checkDigits shouldBe checkDigits

        val leiString = s"${lou}${entity}${checkDigits}"
        lei.value shouldBe leiString
      }
    }

    describe("Conforming LEIs from ISIN_LEI_20210209.csv") {
      conformingCases.foreach { value =>
        describe(s"Conforming LEI '$value'") {
          it("should parse correctly") {
            val lei = Lei.fromString(value).toOption.get
            lei.isConforming shouldBe true
          }

          it("should validate correctly") {
            Lei.validate(value) shouldBe true
          }

          it("should validate when allowing non-conforming") {
            Lei.validateAllowNonConforming(value) shouldBe true
          }
        }
      }
    }

    describe("Non-conforming LEIs with *00 and *01 formats") {
      nonConformingCases.foreach { value =>
        describe(s"Non-conforming LEI '$value'") {
          it("should parse correctly") {
            val lei = Lei.fromString(value).toOption.get
            lei.isConforming shouldBe false
          }

          it("should fail validation") {
            Lei.validate(value) shouldBe false
          }

          it("should validate when allowing non-conforming") {
            Lei.validateAllowNonConforming(value) shouldBe true
          }

          /*
           * NOTE: These identifiers from the GLEIF data file are NOT valid because they end in "00" or "01" which subsection
           * 5.1 of the ISO standard explicitly disallow along with "99". However, if you only run the check-digit-validation
           * step without rejecting them a priori, then they will appear to be valid LEIs (if the compute_iso7064_mod97_10
           * method returns 1 for a 20-digit LEI then it is possibly valid (although you are also supposed to check that the
           * check digits are in the range [02-98] also. This test is here to demonstrate that the bad LEIs in the GLEIF data
           * are probably there because the range check was not done to further validate purported LEIs that passed the
           * "MOD 97-10" check alone.
           */
          it("should get 1 for compute_iso7064_mod97_10") {
            val result = IsoIec7064.mod97_10(value)
            result shouldBe 1
          }
        }
      }
    }

    it("should correctly support default Ordering") {
      val a = Lei.fromString("635400B4JJBON4TCHF02").toOption.get
      val b = Lei.fromString("529900ODI3047E2LIV03").toOption.get
      val seq = Seq(a, b)
      val sorted = seq.sorted
      sorted shouldBe Seq(b, a)
    }

    it("should correctly support pattern matching") {
      val a = Lei.fromString("635400B4JJBON4TCHF02").toOption.get
      val b = Lei.fromString("529900ODI3047E2LIV03").toOption.get
      val seq = Seq(a, b).map { case Lei(v) => v }
      seq shouldBe Seq("635400B4JJBON4TCHF02", "529900ODI3047E2LIV03")
    }

  }

}
