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

import scala.util.matching.Regex

/** An ISO 10383 2012 identifier. The format is exactly four alphanumeric characters with no internal structure.
  *
  * @see
  *   https://en.wikipedia.org/wiki/Market_Identifier_Code
  */
final case class Mic private (value: String) {
  override def toString: String = value
  def toStringTagged: String = s"mic:$value"
}

object Mic {

  implicit val ord: Ordering[Mic] = Ordering.by(_.value)

  /** This will only consider `value` valid if it has no whitespace, all letters are already uppercase, the length is 4
    * and all characters are ASCII alphanumeric.
    */
  val MicFormat: Regex = "([A-Z0-9]{4})".r

  /** Attempt to parse `value` as a MIC, using loose validation. */
  def fromString(value: String): Either[String, Mic] =
    normalize(value) match {
      case MicFormat(v) => Right(new Mic(v))
      case _            => Left(s"MIC value '$value' is not exactly four (4) uppercase alphanumerics")
    }

  def isValidFormatStrict(string: String): Boolean =
    MicFormat.matches(string)

  def isValidFormatLoose(string: String): Boolean =
    MicFormat.matches(normalize(string))

}
