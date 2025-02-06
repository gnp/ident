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

import zio.json.*
import zio.test.*
import zio.test.Assertion.*

import IdentZioJson.*

object IdentZioJsonSpec extends ZIOSpecDefault {

  val isinString = "US0378331005"
  val isinJsonString = s""""$isinString""""

  def spec: Spec[Any, Any] = suite("IdentZioJsonSpec")(
    test("Correctly parse and validate the example AAPL ISIN from the isin.org web site") {
      val expected = Isin.fromString(isinString).toOption.get
      val result = isinJsonString.fromJson[Isin]
      assert(result)(isRight(equalTo(expected)))
    },
    test("Correctly fail to parse an invalid JSON") {
      val expected = "(expected '\"' got '5')"
      val result = "53".fromJson[Isin]
      assert(result)(isLeft(equalTo(expected)))
    }
  )

}
