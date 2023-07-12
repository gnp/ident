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

import io.circe.Decoder
import io.circe.Encoder

object IdentCirce {

  implicit val cikCirceEncoder: Encoder[Cik] = Encoder.encodeLong.contramap(_.value)
  implicit val cikCirceDecoder: Decoder[Cik] =
    Decoder.decodeLong.emap(Cik.fromLong).or(Decoder.decodeString.emap(Cik.fromString))

  implicit val cusipCirceEncoder: Encoder[Cusip] = Encoder.encodeString.contramap(_.value)
  implicit val cusipCirceDecoder: Decoder[Cusip] = Decoder.decodeString.emap(Cusip.fromString)

  implicit val figiCirceEncoder: Encoder[Figi] = Encoder.encodeString.contramap(_.value)
  implicit val figiCirceDecoder: Decoder[Figi] = Decoder.decodeString.emap(Figi.fromString)

  implicit val isinCirceEncoder: Encoder[Isin] = Encoder.encodeString.contramap(_.value)
  implicit val isinCirceDecoder: Decoder[Isin] = Decoder.decodeString.emap(Isin.fromString)

  implicit val leiCirceEncoder: Encoder[Lei] = Encoder.encodeString.contramap(_.value)
  implicit val leiCirceDecoder: Decoder[Lei] = Decoder.decodeString.emap(Lei.fromString)

  implicit val micCirceEncoder: Encoder[Mic] = Encoder.encodeString.contramap(_.value)
  implicit val micCirceDecoder: Decoder[Mic] = Decoder.decodeString.emap(Mic.fromString)

}
