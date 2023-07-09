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
import zio.test.Assertion.*
import zio.test.*

object CikSpec extends ZIOSpecDefault {

  def spec: Spec[Any, Any] = suite("CikSpec")(
    test("Correctly allow the AAPL CIK as a Long") {
      val cik = Cik.fromLong(320193L).toOption.get

      assert(cik.value)(equalTo(320193L))
      assert(cik.toString)(equalTo("320193"))
      assert(cik.toStringPadded)(equalTo("0000320193"))
      assert(cik.toStringTagged)(equalTo("cik:320193"))
    },
    test("Correctly allow the AAPL CIK as a String") {
      val cik = Cik.fromString("320193").toOption.get

      assert(cik.value)(equalTo(320193L))
      assert(cik.toString)(equalTo("320193"))
      assert(cik.toStringPadded)(equalTo("0000320193"))
      assert(cik.toStringTagged)(equalTo("cik:320193"))
    },
    test("Correctly allow the AAPL CIK as a String with leading zeros") {
      val cik = Cik.fromString("000320193").toOption.get

      assert(cik.value)(equalTo(320193L))
      assert(cik.toString)(equalTo("320193"))
      assert(cik.toStringPadded)(equalTo("0000320193"))
      assert(cik.toStringTagged)(equalTo("cik:320193"))
    },
    test("Correctly support default Ordering") {
      val aapl = Cik.fromLong(320193L).toOption.get
      val ibm = Cik.fromLong(51143L).toOption.get
      val chunk = Chunk(aapl, ibm)
      val sorted = chunk.sorted
      assert(sorted)(equalTo(Chunk(ibm, aapl)))
    },
    test("Correctly support pattern matching") {
      val aapl = Cik.fromLong(320193L).toOption.get
      val ibm = Cik.fromLong(51143L).toOption.get
      val chunk = Chunk(aapl, ibm).map { case Cik(v) => v }
      assert(chunk)(equalTo(Chunk(320193L, 51143L)))
    }
  )

}
