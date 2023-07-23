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

import scala.annotation.unused

object IdentZioSchema {

  /** Example of using [[Schema.either]].
    *
    * @todo
    *   This is here for comparison with [[cikZioSchemaOrElse]]. It is not intended to be used and is intended to be
    *   removed when we have fallback support in ZIO Schema.
    *
    * @note
    *   This does not work because the [[JsonDecoder]] expects Either[L, R] to be { left: _, right: _ } or something
    *   like that. That makes sense because the underlying Schema was defined as `Either`, so it is trying to literally
    *   decode an Either, which it has to assume for full generality is a JSON Object with a single member, the key for
    *   which determines whether the value should be decoded as a `Left` or a `Right`.
    */
  @unused
  private[ident] val cikZioSchemaEither: Schema[Cik] =
    Schema
      .either(Schema.primitive[String], Schema.primitive[Long])
      .transformOrFail(
        _ match {
          case Left(s)  => Cik.fromString(s)
          case Right(l) => Cik.fromLong(l)
        },
        ident => Right(Right(ident.value))
      )

  /** Example of using [[Schema.orElseEither]].
    *
    * Intended to transcodes between either Long or String and Cik, but does not.
    *
    * @todo
    *   This is intended to be the primary version but cannot be until ZIO Schema supports fallback semantics. Until
    *   then, this implementation is here to remind us to check up on the status of the issue filed with ZIO Schema. See
    *   link below.
    *
    * @note
    *   This does not work because ZIO Schema internally maps fallback semantics you'd expect from something named
    *   `orElse*` ("I want an `A`, and here are two ways to get it") to the same representation as "I want a result that
    *   is literally of type `Either[A, B]`". Thus, all the [[JsonDecoder]] sees is that it should try to decode
    *   `Either[Long, String]`, which it tries to do as a JSON Object like "`{ left: 42 }`" or "`{ right: "42" }`".
    *
    * @see
    *   https://github.com/zio/zio-schema/issues/584
    */
  @unused
  private[ident] val cikZioSchemaOrElse: Schema[Cik] = Schema
    .primitive[Long]
    .orElseEither(Schema.primitive[String])
    .transformOrFail(
      _ match {
        case Right(s) => Cik.fromString(s)
        case Left(l)  => Cik.fromLong(l)
      },
      ident => Right(Left(ident.value))
    )

  /** Transcodes between String and Cik. This is an alternate version because Cik is numeric. You have to use this one
    * explicitly if you want it. It does not participate in implicit resolution.
    */
  val cikZioSchemaString: Schema[Cik] =
    Schema.primitive[String].transformOrFail(value => Cik.fromString(value), ident => Right(ident.value.toString))

  /** Transcodes between Long and Cik.
    */
  val cikZioSchemaLong: Schema[Cik] =
    Schema.primitive[Long].transformOrFail(value => Cik.fromLong(value), ident => Right(ident.value))

  /** Transcodes between Long and Cik.
    *
    * @note
    *   This is intended to be switched from an alias for [[cikZioSchemaLong]] to a new version that supports both Long
    *   and String once ZIO Schema supports fallback semantics.
    */
  implicit val cikZioSchema: Schema[Cik] = cikZioSchemaLong

  /** Transcodes between String and Cusip.
    *
    * @note
    *   This is intended to be replaced as the
    */
  implicit val cusipZioSchema: Schema[Cusip] =
    Schema
      .primitive[String]
      .transformOrFail(value => Cusip.fromString(value).left.map(_.toString), ident => Right(ident.value))

  /** Transcodes between String and Figi */
  implicit val figiZioSchema: Schema[Figi] =
    Schema.primitive[String].transformOrFail(value => Figi.fromString(value), ident => Right(ident.value))

  /** Transcodes bretween String and Isin */
  implicit val isinZioSchema: Schema[Isin] =
    Schema.primitive[String].transformOrFail(value => Isin.fromString(value), ident => Right(ident.value))

  /** Transcodes between String and Lei */
  implicit val leiZioSchema: Schema[Lei] =
    Schema.primitive[String].transformOrFail(value => Lei.fromString(value), ident => Right(ident.value))

  /** Transcodes between String and Mic */
  implicit val micZioSchema: Schema[Mic] =
    Schema.primitive[String].transformOrFail(value => Mic.fromString(value), ident => Right(ident.value))

}
