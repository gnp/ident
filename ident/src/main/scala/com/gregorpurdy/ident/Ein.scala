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

/** Does not support "plan" or other suffixes
  *
  * @see
  *   https://www.irs.gov/businesses/small-businesses-self-employed/employer-id-numbers
  *
  * @see
  *   https://www.irs.gov/businesses/small-businesses-self-employed/how-eins-are-assigned-and-valid-ein-prefixes
  */
case class Ein private (prefix: String, body: String) {
  assert(prefix.length == 2)
  assert(body.length == 7)

  override def toString: String = s"$prefix-$body"

  def toIdentifierString: String = s"ein:$prefix-$body"

}

/** A U.S. Internal Revenue Service Tax Identification Number for an employer, notionally of the format 99-9999999.
  */
object Ein {

  def unapply(ein: Ein): (String, String) = (ein.prefix, ein.body)

  implicit val ord: Ordering[Ein] = Ordering.by(unapply)

  val prefixFormat: Regex = """([0-9]{1,2})""".r
  val bodyFormat: Regex = """([0-9]{7})""".r

  val einFormat: Regex = """([0-9]{1,2})-?([0-9]{7})""".r

  /** Parse EIN from String, allowing for the possibility of empty String or other input to consider as "No EIN".
    */
  def parseOptional(string: String): Either[String, Option[Ein]] = {
    normalize(string) match {
      case einFormat(p, "0000000") if p == "0" || p == "00" => Right(None)
      case einFormat(p, "1111111") if p == "1" || p == "01" => Right(None)
      case einFormat(p, "4444444") if p == "4" || p == "04" => Right(None)
      case einFormat("11", "1111111")                       => Right(None)
      case einFormat("88", "8888888")                       => Right(None)
      case einFormat("99", "9999999")                       => Right(None)

      case einFormat(prefix, body) if prefix.length == 1 => Right(Some(Ein("0" + prefix, body)))
      case einFormat(prefix, body)                       => Right(Some(Ein(prefix, body)))

      case _ => Left(s"Invalid EIN format: '$string'")
    }
  }

  /** Parse EIN from String, not allowing "No EIN" cases.
    */
  def parse(string: String): Either[String, Ein] = {
    parseOptional(string) match {
      case Left(msg)        => Left(msg)
      case Right(None)      => Left(s"Expected EIN, but got None: '$string'")
      case Right(Some(ein)) => Right(ein)
    }
  }

  /** Parse EIN from String, not allowing "No EIN" cases, and throwing an exception for any problems.
    */
  def apply(string: String): Ein = {
    parse(string) match {
      case Left(msg)  => throw new IllegalArgumentException(msg)
      case Right(ein) => ein
    }
  }

  def validatePrefixStrict(string: String): Either[String, String] = {
    string match {
      case prefixFormat(prefix) if prefix.length == 1 => Right("0" + string)
      case prefixFormat(prefix) if prefix.length == 2 => Right(string)
      case _                                          => Left(s"Invalid EIN prefix: '$string'")
    }
  }

  def validateBodyStrict(string: String): Either[String, String] = {
    string match {
      case bodyFormat(body) => Right(body)
      case _                => Left(s"Invalid EIN body: '$string'")
    }
  }

  def fromParts(prefix: String, body: String): Either[String, Ein] = {
    for {
      p <- validatePrefixStrict(normalize(prefix))
      _ <- validateBodyStrict(normalize(body))
    } yield Ein(p, body)
  }

}
