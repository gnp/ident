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

object IdentZioSchema {

  implicit val cikZioSchemaLong: Schema[Cik] =
    Schema.primitive[Long].transformOrFail(value => Cik.fromLong(value), cik => Right(cik.value))

  implicit val cusipZioSchema: Schema[Cusip] =
    Schema.primitive[String].transformOrFail(string => Cusip.fromString(string), cusip => Right(cusip.value))

  // FIXME: How to combine cikZioSchemaLong and cikZioSchemaString into fallback so we can flexibly accept Long or String input for decode?
  // implicit val cikZioSchemaString: Schema[Cik] =
  //   Schema.primitive[String].transformOrFail(value => Cik.fromString(value), cik => Right(cik.value.toString))

  implicit val isinZioSchema: Schema[Isin] =
    Schema.primitive[String].transformOrFail(string => Isin.fromString(string), isin => Right(isin.value))

}
