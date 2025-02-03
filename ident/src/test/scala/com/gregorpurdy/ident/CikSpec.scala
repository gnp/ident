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

class CikSpec extends AnyFunSpec with should.Matchers {

  describe("Cik") {
    it("should correctly allow the AAPL CIK as a Long") {
      val cik = Cik.fromLong(320193L).toOption.get

      cik.value should be(320193L)
      cik.toString should be("320193")
      cik.toStringPadded should be("0000320193")
      cik.toStringTagged should be("cik:320193")
    }

    it("should correctly allow the AAPL CIK as a String") {
      val cik = Cik.fromString("320193").toOption.get

      cik.value should be(320193L)
      cik.toString should be("320193")
      cik.toStringPadded should be("0000320193")
      cik.toStringTagged should be("cik:320193")
    }

    it("should correctly allow the AAPL CIK as a String with leading zeros") {
      val cik = Cik.fromString("000320193").toOption.get

      cik.value should be(320193L)
      cik.toString should be("320193")
      cik.toStringPadded should be("0000320193")
      cik.toStringTagged should be("cik:320193")
    }

    it("should correctly support default Ordering") {
      val aapl = Cik.fromLong(320193L).toOption.get
      val ibm = Cik.fromLong(51143L).toOption.get
      val seq = Seq(aapl, ibm)
      val sorted = seq.sorted
      sorted should be(Seq(ibm, aapl))
    }

    it("should correctly support pattern matching") {
      val aapl = Cik.fromLong(320193L).toOption.get
      val ibm = Cik.fromLong(51143L).toOption.get
      val seq = Seq(aapl, ibm).map { case Cik(v) => v }
      seq should be(Seq(320193L, 51143L))
    }
  }

}
