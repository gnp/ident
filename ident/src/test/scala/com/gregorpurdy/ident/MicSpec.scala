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

class MicSpec extends AnyFunSpec with should.Matchers {

  describe("Mic") {
    it("Correctly allow the NASDAQ MIC") {
      val mic = Mic.fromString("XNAS").toOption.get

      mic.value shouldBe "XNAS"
      mic.toString shouldBe "XNAS"
      mic.toStringTagged shouldBe "mic:XNAS"
    }

    it("Correctly allow the NYSE MIC") {
      val mic = Mic.fromString("XNYS").toOption.get

      mic.value shouldBe "XNYS"
      mic.toString shouldBe "XNYS"
      mic.toStringTagged shouldBe "mic:XNYS"
    }

    it("Correctly support default Ordering") {
      val nyse = Mic.fromString("XNYS").toOption.get
      val nasdaq = Mic.fromString("XNAS").toOption.get
      val seq = Seq(nyse, nasdaq)
      val sorted = seq.sorted
      sorted shouldBe Seq(nasdaq, nyse)
    }

    it("Correctly support pattern matching") {
      val nyse = Mic.fromString("XNYS").toOption.get
      val nasdaq = Mic.fromString("XNAS").toOption.get
      val seq = Seq(nyse, nasdaq).map { case Mic(v) => v }
      seq shouldBe Seq("XNYS", "XNAS")
    }
  }

}
