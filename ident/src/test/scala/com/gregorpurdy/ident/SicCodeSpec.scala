/*
 * Copyright 2025 Gregor Purdy
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

class SicCodeSpec extends AnyFunSpec with should.Matchers {

  describe("SicCode") {
    it("should correctly parse SIC 'A' as a Division") {
      val sic = SicCode("A") // Agriculture, Forestry, and Fishing
      assert(sic.isInstanceOf[SicDivisionCode])
      assert(sic.code === "A")
      assert(sic.toString === "A")
      assert(sic.toIdentifierString === "sic:A")
    }

    it("should correctly parse SIC 'a' as a Division") {
      val sic = SicCode("a") // Agriculture, Forestry, and Fishing
      assert(sic.isInstanceOf[SicDivisionCode])
      assert(sic.asInstanceOf[SicDivisionCode].id === 1)
      assert(sic.code === "A")
      assert(sic.toString === "A")
      assert(sic.toIdentifierString === "sic:A")
    }

    it("should correctly parse SIC '*' as a Division") {
      val sic = SicCode("*") // OES Survey of Federal, State, and Local Government
      assert(sic.isInstanceOf[SicDivisionCode])
      assert(sic.asInstanceOf[SicDivisionCode].id === 27)
      assert(sic.code === "*")
      assert(sic.toString === "*")
      assert(sic.toIdentifierString === "sic:*")
    }

    it("should correctly parse four-digit SIC '0100' as a Major Group") {
      val sic = SicCode("0100") // Agricultural Production Crops
      assert(sic.isInstanceOf[SicMajorGroupCode])
      assert(sic.asInstanceOf[SicMajorGroupCode].id === 1)
      assert(sic.code === "01")
      assert(sic.toString === "01")
      assert(sic.toIdentifierString === "sic:01")
    }

    it("should correctly parse three-digit SIC '010' as a Major Group") {
      val sic = SicCode("010") // Agricultural Production Crops
      assert(sic.isInstanceOf[SicMajorGroupCode])
      assert(sic.asInstanceOf[SicMajorGroupCode].id === 1)
      assert(sic.code === "01")
      assert(sic.toString === "01")
      assert(sic.toIdentifierString === "sic:01")
    }

    it("should correctly parse two-digit SIC '01' as a Major Group") {
      val sic = SicCode("01") // Agricultural Production Crops
      assert(sic.isInstanceOf[SicMajorGroupCode])
      assert(sic.asInstanceOf[SicMajorGroupCode].id === 1)
      assert(sic.code === "01")
      assert(sic.toString === "01")
      assert(sic.toIdentifierString === "sic:01")
    }

    it("should correctly interpret SICIndustry(131) as SICIndustry(\"0131\")") {
      val sic = SicIndustryCode(131)
      assert(sic.isInstanceOf[SicIndustryCode])
      assert(sic.asInstanceOf[SicIndustryCode].id === 131)
      assert(sic.code === "0131")
    }

    it("should correctly interpret SICIndustry(1300) as SICMajorGroup(\"13\")") {
      val sic = SicIndustryCode(1300)
      assert(sic.isInstanceOf[SicMajorGroupCode])
      assert(sic.asInstanceOf[SicMajorGroupCode].id === 13)
      assert(sic.code === "13")
    }

    it("should correctly interpret SICIndustry(130) as SICIndustryGroup(\"013\")") {
      val sic = SicIndustryCode(130)
      assert(sic.isInstanceOf[SicIndustryGroupCode])
      assert(sic.asInstanceOf[SicIndustryGroupCode].id === 13)
      assert(sic.code === "013")
    }

    // it("should orrectly allow SIC 9995") {
    //   val sic = SIC("9995") // NON-OPERATING ESTABLISHMENTS
    //
    //   assert(sic.code === "9995")
    //   assert(sic.toString === "9995")
    // }

  }

}
