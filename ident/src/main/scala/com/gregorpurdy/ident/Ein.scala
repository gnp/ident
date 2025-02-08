/*
 * Copyright 2025 Gregor Purdy
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

import scala.util.matching.Regex

/** A U.S. Internal Revenue Service Tax Identification Number for an employer, notionally of the format 99-9999999.
  *
  * Does not support "plan" or other suffixes
  *
  * @see
  *   https://www.irs.gov/businesses/small-businesses-self-employed/employer-id-numbers
  *
  * @see
  *   https://www.irs.gov/businesses/small-businesses-self-employed/how-eins-are-assigned-and-valid-ein-prefixes
  */
enum Ein {
  case Normal private[Ein] (value: String)
  case Reserved private[Ein] (value: String)

  def body: String = value.substring(3)

  def prefix: String = value.substring(0, 2)

  def toIdentifierString: String =
    s"ein:$value"

  override def toString: String =
    value

  def value: String

}

object Ein {

  implicit val ord: Ordering[Ein] = Ordering.by(_.value)

  val prefixFormat: Regex = """([0-9]{1,2})""".r
  val bodyFormat: Regex = """([0-9]{7})""".r

  val einFormat: Regex = """([0-9]{1,2})-?([0-9]{7})""".r

  def fromParts(prefix: String, body: String): Either[String, Ein] =
    for {
      p <- validatePrefixStrict(normalize(prefix))
      b <- validateBodyStrict(normalize(body))
      s = s"$p-$b"
      e <- fromStringStrict(s)
    } yield e

  def fromString(value: String): Either[String, Ein] =
    fromStringStrict(normalize(value))

  /** Parse EIN from String.
    */
  def fromStringStrict(value: String): Either[String, Ein] = {
    value match {
      case einFormat(p, "0000000") if p == "0" || p == "00" => Right(Reserved("00-0000000"))
      case einFormat(p, "1111111") if p == "1" || p == "01" => Right(Reserved("01-1111111"))
      case einFormat(p, "4444444") if p == "4" || p == "04" => Right(Reserved("04-4444444"))
      case einFormat("11", "1111111")                       => Right(Reserved("11-1111111"))
      case einFormat("88", "8888888")                       => Right(Reserved("88-8888888"))
      case einFormat("99", "9999999")                       => Right(Reserved("99-9999999"))

      case einFormat(prefix, body) if prefix.length == 1 => Right(Normal("0" + prefix + "-" + body))
      case einFormat(prefix, body)                       => Right(Normal(prefix + "-" + body))

      case _ => Left(s"Invalid EIN format: '$value'")
    }
  }

  def validateBodyStrict(string: String): Either[String, String] =
    string match {
      case bodyFormat(body) => Right(body)
      case _                => Left(s"Invalid EIN body: '$string'")
    }

  def validatePrefixStrict(string: String): Either[String, String] =
    string match {
      case prefixFormat(prefix) if prefix.length == 1 => Right("0" + string)
      case prefixFormat(prefix) if prefix.length == 2 => Right(string)
      case _                                          => Left(s"Invalid EIN prefix: '$string'")
    }

}
