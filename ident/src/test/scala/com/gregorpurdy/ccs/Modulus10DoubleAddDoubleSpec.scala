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

package com.gregorpurdy.ccs

import zio.test.Assertion.*
import zio.test.*

import Modulus10DoubleAddDouble.*

object Modulus10DoubleAddDoubleSpec extends ZIOSpecDefault {
  def spec: Spec[Any, Any] = suite("Modulus10DoubleAddDoubleSpec")(
    test("CusipVariant on CUSIP-length (8) payloads") {
      check(Gen.alphaNumericStringBounded(8, 8)) { s =>
        val payload = s.toUpperCase
        assert(CusipVariant.calculate(payload))(equalTo(CusipVariant.calculateSimple(payload)))
      }
    } @@ TestAspect.samples(1000),
    test("CusipVariant on FIGI-length (11) payloads") {
      check(Gen.alphaNumericStringBounded(11, 11)) { s =>
        val payload = s.toUpperCase
        assert(CusipVariant.calculate(payload))(equalTo(CusipVariant.calculateSimple(payload)))
      }
    } @@ TestAspect.samples(1000),
    test("CusipVariant on length 1 payloads") {
      check(Gen.alphaNumericStringBounded(1, 1)) { s =>
        val payload = s.toUpperCase
        assert(CusipVariant.calculate(payload))(equalTo(CusipVariant.calculateSimple(payload)))
      }
    },
    test("CusipVariant on length 2 payloads") {
      check(Gen.alphaNumericStringBounded(2, 2)) { s =>
        val payload = s.toUpperCase
        assert(CusipVariant.calculate(payload))(equalTo(CusipVariant.calculateSimple(payload)))
      }
    },
    test("CusipVariant on length 1-100 payloads") {
      check(Gen.alphaNumericStringBounded(1, 100)) { s =>
        val payload = s.toUpperCase
        assert(CusipVariant.calculate(payload))(equalTo(CusipVariant.calculateSimple(payload)))
      }
    },
    test("IsinVariant on ISIN-length (11) payloads") {
      check(Gen.alphaNumericStringBounded(11, 11)) { s =>
        val payload = s.toUpperCase
        assert(IsinVariant.calculate(payload))(equalTo(IsinVariant.calculateSimple(payload)))
      }
    } @@ TestAspect.samples(1000),
    test("IsinVariant on length 1 payloads") {
      check(Gen.alphaNumericStringBounded(1, 1)) { s =>
        val payload = s.toUpperCase
        assert(IsinVariant.calculate(payload))(equalTo(IsinVariant.calculateSimple(payload)))
      }
    },
    test("IsinVariant on length 2 payloads") {
      check(Gen.alphaNumericStringBounded(2, 2)) { s =>
        val payload = s.toUpperCase
        assert(IsinVariant.calculate(payload))(equalTo(IsinVariant.calculateSimple(payload)))
      }
    },
    test("IsinVariant on length 1-100 payloads") {
      check(Gen.alphaNumericStringBounded(1, 100)) { s =>
        val payload = s.toUpperCase
        assert(IsinVariant.calculate(payload))(equalTo(IsinVariant.calculateSimple(payload)))
      }
    }
  )

}
