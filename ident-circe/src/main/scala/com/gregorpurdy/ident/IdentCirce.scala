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


  implicit val isinCirceEncoder: Encoder[Isin] = Encoder.encodeString.contramap(_.value)
  implicit val isinCirceDecoder: Decoder[Isin] = Decoder.decodeString.emap(Isin.fromString)

}
