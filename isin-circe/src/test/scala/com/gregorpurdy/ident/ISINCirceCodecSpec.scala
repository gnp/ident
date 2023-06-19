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

import io.circe.parser.decode
import zio.test.Assertion.*
import zio.test.*

import ISINCirceCodec.*

object ISINCirceCodecSpec extends ZIOSpecDefault {

  val isinString = "US0378331005"
  val isinJsonString = s""""$isinString""""

  def spec: Spec[Any, Any] = suite("ISINCirceCodecSpec")(
    test("Correctly parse and validate the example AAPL ISIN from the isin.org web site") {
      val result = decode[ISIN](isinJsonString)

      assert(result)(equalTo(Right(ISIN.parse(isinString).toOption.get)))
    },
    test("Correctly fail to parse an invalid JSON") {
      val expected: Either[String, ISIN] =
        Left("DecodingFailure at : Got value '53' with wrong type, expecting string")
      val result = decode[ISIN]("53").swap.map(_.getMessage).swap

      assert(result)(equalTo(expected))
    }
  )

}
