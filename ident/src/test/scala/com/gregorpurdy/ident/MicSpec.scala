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

import zio.Chunk
import zio.test.*
import zio.test.Assertion.*

object MicSpec extends ZIOSpecDefault {

  def spec: Spec[Any, Any] = suite("MicSpec")(
    test("Correctly allow the NASDAQ MIC") {
      val mic = Mic.fromString("XNAS").toOption.get

      assert(mic.value)(equalTo("XNAS"))
      assert(mic.toString)(equalTo("XNAS"))
      assert(mic.toStringTagged)(equalTo("mic:XNAS"))
    },
    test("Correctly allow the NYSE MIC") {
      val mic = Mic.fromString("XNYS").toOption.get

      assert(mic.value)(equalTo("XNYS"))
      assert(mic.toString)(equalTo("XNYS"))
      assert(mic.toStringTagged)(equalTo("mic:XNYS"))
    },
    test("Correctly support default Ordering") {
      val nyse = Mic.fromString("XNYS").toOption.get
      val nasdaq = Mic.fromString("XNAS").toOption.get
      val chunk = Chunk(nyse, nasdaq)
      val sorted = chunk.sorted
      assert(sorted)(equalTo(Chunk(nasdaq, nyse)))
    },
    test("Correctly support pattern matching") {
      val nyse = Mic.fromString("XNYS").toOption.get
      val nasdaq = Mic.fromString("XNAS").toOption.get
      val chunk = Chunk(nyse, nasdaq).map { case Mic(v) => v }
      assert(chunk)(equalTo(Chunk("XNYS", "XNAS")))
    }
  )

}
