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

import zio.json.*

object IdentZioJson {

  implicit val cikZioJsonDecoder: JsonDecoder[Cik] =
    JsonDecoder[Long].mapOrFail(Cik.fromLong).orElse(JsonDecoder[String].mapOrFail(Cik.fromString))
  implicit val cikZioJsonEncoder: JsonEncoder[Cik] = JsonEncoder[Long].contramap(_.value)

  implicit val cusipZioJsonDecoder: JsonDecoder[Cusip] =
    JsonDecoder[String].mapOrFail(s => Cusip.fromString(s).left.map(_.toString))
  implicit val cusipZioJsonEncoder: JsonEncoder[Cusip] = JsonEncoder[String].contramap(_.value)

  implicit val figiZioJsonDecoder: JsonDecoder[Figi] = JsonDecoder[String].mapOrFail(Figi.fromString)
  implicit val figiZioJsonEncoder: JsonEncoder[Figi] = JsonEncoder[String].contramap(_.value)

  implicit val isinZioJsonDecoder: JsonDecoder[Isin] =
    JsonDecoder[String].mapOrFail(s => Isin.fromString(s).left.map(_.toString))
  implicit val isinZioJsonEncoder: JsonEncoder[Isin] = JsonEncoder[String].contramap(_.value)

  implicit val leiZioJsonDecoder: JsonDecoder[Lei] = JsonDecoder[String].mapOrFail(Lei.fromString)
  implicit val leiZioJsonEncoder: JsonEncoder[Lei] = JsonEncoder[String].contramap(_.value)

  implicit val micZioJsonDecoder: JsonDecoder[Mic] = JsonDecoder[String].mapOrFail(Mic.fromString)
  implicit val micZioJsonEncoder: JsonEncoder[Mic] = JsonEncoder[String].contramap(_.value)

}
