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

class EinSpec extends AnyFunSpec with should.Matchers {

  describe("Ein") {
    it("should handle Normal cases") {
      Ein.fromString("12-3456789").toOption.get shouldBe a[Ein.Normal]
      Ein.fromString("12-3456789").toOption.get.value shouldBe "12-3456789"

      Ein.fromString("234567890").toOption.get shouldBe a[Ein.Normal]
      Ein.fromString("234567890").toOption.get.value shouldBe "23-4567890"

      Ein.fromString("03-4567890").toOption.get shouldBe a[Ein.Normal]
      Ein.fromString("03-4567890").toOption.get.value shouldBe "03-4567890"

      Ein.fromString("4-5678901").toOption.get shouldBe a[Ein.Normal]
      Ein.fromString("4-5678901").toOption.get.value shouldBe "04-5678901"
    }

    it("should handle Reserved cases") {
      Ein.fromString("0-0000000").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("0-0000000").toOption.get.value shouldBe "00-0000000"

      Ein.fromString("1-1111111").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("1-1111111").toOption.get.value shouldBe "01-1111111"

      Ein.fromString("4-4444444").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("4-4444444").toOption.get.value shouldBe "04-4444444"

      Ein.fromString("00-0000000").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("00-0000000").toOption.get.value shouldBe "00-0000000"

      Ein.fromString("01-1111111").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("01-1111111").toOption.get.value shouldBe "01-1111111"

      Ein.fromString("04-4444444").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("04-4444444").toOption.get.value shouldBe "04-4444444"

      Ein.fromString("11-1111111").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("11-1111111").toOption.get.value shouldBe "11-1111111"

      Ein.fromString("88-8888888").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("88-8888888").toOption.get.value shouldBe "88-8888888"

      Ein.fromString("99-9999999").toOption.get shouldBe a[Ein.Reserved]
      Ein.fromString("99-9999999").toOption.get.value shouldBe "99-9999999"
    }

    it("should reject invalid formats") {
      Ein.fromString("123-456789") shouldBe a[Left[?, ?]]
      Ein.fromString("1234-56789") shouldBe a[Left[?, ?]]
      Ein.fromString("ab-1234567") shouldBe a[Left[?, ?]]
    }

    it("should support ordering") {
      val ein1 = Ein.fromParts("12", "3456789").toOption.get
      val ein2 = Ein.fromParts("12", "3456790").toOption.get
      val ein3 = Ein.fromParts("13", "3456789").toOption.get

      ein1 should be < ein2
      ein2 should be < ein3
    }

    it("should format correctly") {
      val ein = Ein.fromParts("12", "3456789").toOption.get
      ein.toString shouldBe "12-3456789"
      ein.toIdentifierString shouldBe "ein:12-3456789"
    }
  }

}
