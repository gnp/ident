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

/** Three classes for ISO 3166 country codes, one each for the alpha-2, alpha-3 and numeric-3 formats */
sealed trait CountryCode

/** An ISO 3166 alpha-2 identifier. The format is exactly two alphabetic characters with no internal structure.
  *
  * @see
  *   https://www.iso.org/iso-3166-country-codes.html
  */
case class CountryCodeAlpha2 private (value: String) extends CountryCode {
  override def toString: String = value

  def toIdentifierString: String = s"country:$value"
}

object CountryCodeAlpha2 {

  val format: Regex = "[A-Z]{2}".r

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 2 and all characters are ASCII alphabetic. The apply() method is more permissive, because it will trim leading
    * and/or trailing whitespace and convert to uppercase before validating the CountryCodeAlpha2.
    */
  def isValidFormatStrict(string: String): Boolean =
    format.matches(string)

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidFormatLoose(string: String): Boolean =
    format.matches(string.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase)

  def apply(
      value: String
  ): CountryCodeAlpha2 = {
    val tempValue = value.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase

    if (!isValidFormatStrict(tempValue))
      throw new IllegalArgumentException(
        s"Format of '$value' is not valid"
      )

    new CountryCodeAlpha2(tempValue)
  }

}

/** An ISO 3166 alpha-3 identifier. The format is exactly three alphabetic characters with no internal structure.
  *
  * @see
  *   https://www.iso.org/iso-3166-country-codes.html
  */
case class CountryCodeAlpha3 private (value: String) extends CountryCode {
  override def toString: String = value

  def toIdentifierString: String = s"country:$value"
}

object CountryCodeAlpha3 {

  val format: Regex = "[A-Z]{3}".r

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 3 and all characters are ASCII alphabetic. The apply() method is more permissive, because it will trim leading
    * and/or trailing whitespace and convert to uppercase before validating the CountryCodeAlpha3.
    */
  def isValidFormatStrict(string: String): Boolean =
    format.matches(string)

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidFormatLoose(string: String): Boolean =
    format.matches(string.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase)

  def apply(
      value: String
  ): CountryCodeAlpha3 = {
    val tempValue = value.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase

    if (!isValidFormatStrict(tempValue))
      throw new IllegalArgumentException(
        s"Format of '$value' is not valid"
      )

    new CountryCodeAlpha3(tempValue)
  }

}

/** An ISO 3166 numeric-3 identifier. The format is exactly three numeric characters with no internal structure.
  *
  * @see
  *   https://www.iso.org/iso-3166-country-codes.html
  */
case class CountryCodeNumeric3 private (value: Short) extends CountryCode {
  override def toString: String = f"$value%03d"

  def toIdentifierString: String = f"country:$value%03d"
}

object CountryCodeNumeric3 {

  val format: Regex = "[0-9]{3}".r

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 3 and all characters are ASCII numeric. The apply() method is more permissive, because it will trim leading and/or
    * trailing whitespace and convert to uppercase before validating the CountryCodeNumeric3.
    */
  def isValidFormatStrict(string: String): Boolean =
    format.matches(string)

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidFormatLoose(string: String): Boolean =
    format.matches(string.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase)

  def apply(
      value: String
  ): CountryCodeNumeric3 = {
    val tempValue = value.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase

    if (!isValidFormatStrict(tempValue))
      throw new IllegalArgumentException(
        s"Format of '$value' is not valid"
      )

    new CountryCodeNumeric3(tempValue.toShort)
  }

  def apply(
      value: Short
  ): CountryCodeNumeric3 = {
    if (value < 0 || value > 999)
      throw new IllegalArgumentException(
        s"Format of '$value' is not valid"
      )

    new CountryCodeNumeric3(value)
  }

}

object CountryCode {

  def apply(value: String): CountryCode = {
    val temp = value.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase

    if (CountryCodeAlpha2.format.matches(temp))
      CountryCodeAlpha2(temp)
    else if (CountryCodeAlpha3.format.matches(temp))
      CountryCodeAlpha3(temp)
    else if (CountryCodeNumeric3.format.matches(temp))
      CountryCodeNumeric3(temp.toShort)
    else
      throw new IllegalArgumentException(
        s"Format of '$value' is not valid"
      )
  }

  def apply(value: Short): CountryCode = CountryCodeNumeric3(value)

}
