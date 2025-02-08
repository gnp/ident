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

class CountryCodeSpec extends AnyFunSpec with should.Matchers {

  describe("CountryCode") {
    it("should correctly uppercase an alpha-2 input") {
      val code = CountryCodeAlpha2.fromString("us").toOption.get

      assert(code.value === "US")
    }

    it("should correctly uppercase an alpha-3 input") {
      val code = CountryCodeAlpha3.fromString("usa").toOption.get

      assert(code.value === "USA")
    }

    it("should render numeric codes as strings with leading zeros") {
      val code = CountryCodeNumeric3.fromShort(4: Short).toOption.get

      assert(code.toString === "004")
    }
  }

}
