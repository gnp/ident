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

import zio.schema.Schema
import zio.schema.codec.JsonCodec
import zio.test.Assertion.*
import zio.test.*

object IdentZioSchemaSpec extends ZIOSpecDefault {

  def spec: Spec[Any, Any] =
    suite("IdentZioSchemaSpec")(
      suite("Support JSON decoding")(
        test("Support CIK decoding from JSON Long") {
          val json = """[42, 43]"""
          val list = List(Cik.fromLong(42).toOption.get, Cik.fromLong(43).toOption.get)

          val schema = Schema.list(IdentZioSchema.cikZioSchema)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        },
        test("Support CIK decoding from JSON String") {
          val json = """["42", "43"]"""
          val list = List(Cik.fromString("42").toOption.get, Cik.fromString("43").toOption.get)

          val schema = Schema.list(IdentZioSchema.cikZioSchemaString)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        },
        test("Support CIK decoding from both JSON String and JSON Long") {
          val json = """[42, "43"]"""
          val list = List(Cik.fromLong(42).toOption.get, Cik.fromString("43").toOption.get)

          val schema = Schema.list(IdentZioSchema.cikZioSchemaOrElse)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        } @@ TestAspect.ignore /* FIXME: Awaiting fallback support in ZIO Schema: https://github.com/zio/zio-schema/issues/584 */,
        test("Support CUSIP decoding from JSON String") {
          val xrx = Cusip.fromString("98421M106").toOption.get
          val amd = Cusip.fromString("007903107").toOption.get
          val json = s"""["$xrx", "$amd"]"""
          val list = List(xrx, amd)

          val schema = Schema.list(IdentZioSchema.cusipZioSchema)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        },
        test("Support FIGI decoding from JSON String") {
          val a = Figi.fromString("NRG92C84SB39").toOption.get
          val b = Figi.fromString("BBG000BLNQ16").toOption.get
          val json = s"""["$a", "$b"]"""
          val list = List(a, b)

          val schema = Schema.list(IdentZioSchema.figiZioSchema)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        },
        test("Support ISIN decoding from JSON String") {
          val bcc = Isin.fromString("US09739D1000").toOption.get
          val intc = Isin.fromString("US4581401001").toOption.get
          val json = s"""["$bcc", "$intc"]"""
          val list = List(bcc, intc)

          val schema = Schema.list(IdentZioSchema.isinZioSchema)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        },
        test("Support LEI decoding from JSON String") {
          val a = Lei.fromString("635400B4JJBON4TCHF02").toOption.get
          val b = Lei.fromString("529900ODI3047E2LIV03").toOption.get
          val json = s"""["$a", "$b"]"""
          val list = List(a, b)

          val schema = Schema.list(IdentZioSchema.leiZioSchema)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        },
        test("Support MIC decoding from JSON String") {
          val nyse = Mic.fromString("XNYS").toOption.get
          val nasdaq = Mic.fromString("XNAS").toOption.get
          val json = s"""["$nyse", "$nasdaq"]"""
          val list = List(nyse, nasdaq)

          val schema = Schema.list(IdentZioSchema.micZioSchema)
          val decoder = JsonCodec.jsonDecoder(schema)

          val result = decoder.decodeJson(json)

          assert(result)(isRight(equalTo(list)))
        }
      )
    )

}
