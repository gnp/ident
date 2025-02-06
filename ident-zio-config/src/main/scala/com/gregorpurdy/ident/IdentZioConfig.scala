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

import zio.Config

object IdentZioConfig {

  implicit val cikConfig: Config[Cik] =
    Config.string.mapOrFail[Cik](s => Cik.fromString(s).left.map(e => Config.Error.InvalidData(message = e)))

  implicit val cusipConfig: Config[Cusip] =
    Config.string.mapOrFail[Cusip](s =>
      Cusip.fromString(s).left.map(e => Config.Error.InvalidData(message = e.toString))
    )

  implicit val figiConfig: Config[Figi] =
    Config.string.mapOrFail[Figi](s => Figi.fromString(s).left.map(e => Config.Error.InvalidData(message = e)))

  implicit val isinConfig: Config[Isin] =
    Config.string.mapOrFail[Isin](s => Isin.fromString(s).left.map(e => Config.Error.InvalidData(message = e.toString)))

  implicit val leiConfig: Config[Lei] =
    Config.string.mapOrFail[Lei](s => Lei.fromString(s).left.map(e => Config.Error.InvalidData(message = e)))

  implicit val micConfig: Config[Mic] =
    Config.string.mapOrFail[Mic](s => Mic.fromString(s).left.map(e => Config.Error.InvalidData(message = e)))

}
