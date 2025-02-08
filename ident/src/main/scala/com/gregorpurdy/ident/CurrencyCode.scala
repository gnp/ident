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

/** Two classes for ISO 4217 currency codes, one each for the alpha-3 and one for the numeric-3 formats */
sealed trait CurrencyCode

/** An ISO 4217 alpha-3 identifier. The format is exactly three alphabetic characters with no internal structure.
  */
case class CurrencyCodeAlpha3 private (value: String) extends CurrencyCode {
  override def toString: String = value

  def toIdentifierString: String = s"currency:$value"
}

object CurrencyCodeAlpha3 {

  val format: Regex = "[A-Z]{3}".r

  def fromString(value: String): Either[String, CurrencyCodeAlpha3] =
    fromStringStrict(normalize(value))

  def fromStringStrict(value: String): Either[String, CurrencyCodeAlpha3] =
    if (!isValidFormatStrict(value))
      Left(s"Format of '$value' is not valid")
    else
      Right(new CurrencyCodeAlpha3(value))

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidFormat(string: String): Boolean =
    format.matches(normalize(string))

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 3 and all characters are ASCII alphabetic. The apply() method is more permissive, because it will trim leading
    * and/or trailing whitespace and convert to uppercase before validating the CurrencyCodeAlpha3.
    */
  def isValidFormatStrict(string: String): Boolean =
    format.matches(string)

}

/** An ISO 4217 numeric-3 identifier. The format is exactly three numeric characters with no internal structure.
  */
case class CurrencyCodeNumeric3 private (value: Short) extends CurrencyCode {
  override def toString: String = f"$value%03d"

  def toIdentifierString: String = f"currency:$value%03d"
}

object CurrencyCodeNumeric3 {

  val format: Regex = "[0-9]{3}".r

  def fromShort(value: Short): Either[String, CurrencyCodeNumeric3] =
    if (value < 0 || value > 999)
      Left(s"Currency code numeric value must be between 0 and 999: $value")
    else
      Right(new CurrencyCodeNumeric3(value))

  def fromString(value: String): Either[String, CurrencyCodeNumeric3] =
    fromStringStrict(normalize(value))

  def fromStringStrict(value: String): Either[String, CurrencyCodeNumeric3] =
    if (!isValidFormatStrict(value))
      Left(s"Format of '$value' is not valid")
    else
      Right(new CurrencyCodeNumeric3(value.toShort))

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidFormatLoose(string: String): Boolean =
    format.matches(string.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase)

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 3 and all characters are ASCII numeric. The apply() method is more permissive, because it will trim leading and/or
    * trailing whitespace and convert to uppercase before validating the CountryCodeNumeric3.
    */
  def isValidFormatStrict(string: String): Boolean =
    format.matches(string)

}

object CurrencyCode {

  def fromString(value: String): Either[String, CurrencyCode] =
    fromStringStrict(normalize(value))

  def fromStringStrict(value: String): Either[String, CurrencyCode] =
    if (CurrencyCodeAlpha3.format.matches(value))
      CurrencyCodeAlpha3.fromStringStrict(value)
    else if (CurrencyCodeNumeric3.format.matches(value))
      CurrencyCodeNumeric3.fromStringStrict(value)
    else
      Left(s"Format of '$value' is not valid")

  def fromShort(value: Short): Either[String, CurrencyCode] =
    CurrencyCodeNumeric3.fromShort(value)

}
