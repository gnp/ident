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

import io.circe.parser.decode
import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*

import IdentCirce.*

class IdentCirceSpec extends AnyFunSpec with should.Matchers {

  val isinString = "US0378331005"
  val isinJsonString = s""""$isinString""""

  describe("IdentCirceSpec") {
    it("Correctly parse and validate the example AAPL ISIN from the isin.org web site") {
      val result = decode[Isin](isinJsonString)
      result shouldBe Right(Isin.fromString(isinString).toOption.get)
    }

    it("Correctly fail to parse an invalid JSON") {
      val expected: Either[String, Isin] =
        Left("DecodingFailure at : Got value '53' with wrong type, expecting string")
      val result = decode[Isin]("53").left.map(_.getMessage)
      result shouldBe expected
    }
  }

}
