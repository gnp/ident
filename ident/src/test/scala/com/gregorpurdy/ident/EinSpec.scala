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
    it("should parse valid EINs") {
      Ein.parse("12-3456789") shouldBe Ein.fromParts("12", "3456789")
      Ein.parse("123456789") shouldBe Ein.fromParts("12", "3456789")
      Ein.parse("02-3456789") shouldBe Ein.fromParts("02", "3456789")
      Ein.parse("2-3456789") shouldBe Ein.fromParts("02", "3456789")
    }

    it("should handle None cases") {
      Ein.parseOptional("0-0000000") shouldBe Right(None)
      Ein.parseOptional("1-1111111") shouldBe Right(None)
      Ein.parseOptional("4-4444444") shouldBe Right(None)

      Ein.parseOptional("00-0000000") shouldBe Right(None)
      Ein.parseOptional("01-1111111") shouldBe Right(None)
      Ein.parseOptional("04-4444444") shouldBe Right(None)

      Ein.parseOptional("11-1111111") shouldBe Right(None)
      Ein.parseOptional("88-8888888") shouldBe Right(None)
      Ein.parseOptional("99-9999999") shouldBe Right(None)
    }

    it("should reject invalid formats") {
      Ein.parse("123-456789") shouldBe a[Left[?, ?]]
      Ein.parse("1234-56789") shouldBe a[Left[?, ?]]
      Ein.parse("ab-1234567") shouldBe a[Left[?, ?]]
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
