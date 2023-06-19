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

/** ISIN stands for International Security Identification Number. It is a String identifer composed of three parts: (1)
  * a two-letter country code, (2) a 9-character alphanumeric security identifier, and (3) a 1-digit check digit. The
  * security identifier portion is allocated by the national body in charge of identifier assignments for the country
  * specified by the leading two-letter code. In the US, the national numbering system is CUSIP.
  *
  * @see
  *   https://www.isin.org/isin-format/
  * @see
  *   https://en.wikipedia.org/wiki/International_Securities_Identification_Number
  */
case class ISIN private (value: String) {
  def countryCode: String = value.substring(0, 2)
  def securityIdentifier: String = value.substring(2, 11)
  def checkDigit: String = value.substring(11, 12)
  def toAssetIdString: String = s"isin:$value"
  override def toString(): String = value
  def toIdentifierString: String = s"isin:$value"
}

object ISIN {

  val countryCodeFormat: Regex = "[A-Z]{2}".r
  val securityIdentifierFormat: Regex = "[A-Z0-9]{9}".r
  val checkDigitFormat: Regex = "[0-9]".r
  val isinFormat: Regex = "([A-Z]{2})([A-Z0-9]{9})([0-9])".r

  def calculateCheckDigit(
      countryCode: String,
      securityIdentifier: String
  ): String = {
    val tempCountryCode = countryCode.trim.toUpperCase
    val tempSecurityIdentifier = securityIdentifier.trim.toUpperCase

    if (!isValidCountryCodeFormatStrict(tempCountryCode))
      throw new IllegalArgumentException(
        s"Format of country code '$countryCode' is not valid"
      )

    if (!isValidSecurityIdentifierFormatStrict(tempSecurityIdentifier))
      throw new IllegalArgumentException(
        s"Format of security identifier '$securityIdentifier' is not valid"
      )

    calculateCheckDigitUnsafe(tempCountryCode, tempSecurityIdentifier)
  }

  /** This method is used internally when the countryCode and securityIdentifier have already been validated to be the
    * right format.
    */
  private def calculateCheckDigitUnsafe(
      countryCode: String,
      securityIdentifier: String
  ): String = {
    def charValue(char: Char): Int = char match {
      case c if c >= '0' && c <= '9' => c - '0'
      case c if c >= 'A' && c <= 'Z' => c - 'A' + 10
      case x =>
        throw new IllegalStateException(
          s"It should not have been possible for this character to make it through: '$x'"
        )
    }

    def timesTwo(x: Int): Seq[Int] = {
      val product = x * 2
      if (product >= 10) Seq(product / 10, product % 10) else Seq(product)
    }

    val temp = (countryCode + securityIdentifier)
      .map(charValue) // Convert characters to their code values (0 - 36)
      .flatMap { x =>
        if (x >= 10) Seq(x / 10, x % 10) else Seq(x)
      } // Convert two-digit codes to two one-digit codes
      .reverse // Start the alternate multiply-by-two and leave-alone from the right
      .zipWithIndex // Pair each number with an index we can use to drive the alternation
      .flatMap { case (x, i) =>
        if (i % 2 == 0) timesTwo(x) else Seq(x)
      } // Double every other one
      .sum

    val diff = 10 - (temp % 10)
    val digit = if (diff == 10) 0 else diff

    digit.toString
  }

  def isValidCountryCodeFormatStrict(string: String): Boolean =
    countryCodeFormat.matches(string)

  def isValidCountryCodeFormatLoose(string: String): Boolean =
    countryCodeFormat.matches(string.trim.toUpperCase)

  def isValidSecurityIdentifierFormatStrict(string: String): Boolean =
    securityIdentifierFormat.matches(string)

  def isValidSecurityIdentifierFormatLoose(string: String): Boolean =
    securityIdentifierFormat.matches(string.trim.toUpperCase)

  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  def isValidCheckDigitFormatLoose(string: String): Boolean =
    checkDigitFormat.matches(string.trim)

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 11 and each component is the right mix of letters and/or digits. The apply() method is more permissive, because it
    * will trim leading and/or trailing whitespace and convert to uppercase before validating the ISIN.
    */
  def isValidIsinFormatStrict(string: String): Boolean =
    isinFormat.matches(string)

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidIsinFormatLoose(string: String): Boolean =
    isinFormat.matches(string.trim.toUpperCase)

  def apply(
      countryCode: String,
      securityIdentifier: String,
      checkDigit: String
  ): ISIN = {
    val tempCountryCode = countryCode.trim.toUpperCase
    val tempSecurityIdentifier = securityIdentifier.trim.toUpperCase
    val tempCheckDigit = checkDigit.trim.toUpperCase

    if (!isValidCountryCodeFormatStrict(tempCountryCode))
      throw new IllegalArgumentException(
        s"Format of country code '$countryCode' is not valid"
      )

    if (!isValidSecurityIdentifierFormatStrict(tempSecurityIdentifier))
      throw new IllegalArgumentException(
        s"Format of security identifier '$securityIdentifier' is not valid"
      )

    if (!isValidCheckDigitFormatStrict(tempCheckDigit))
      throw new IllegalArgumentException(
        s"Format of check digit '$checkDigit' is not valid"
      )

    val correctCheckDigit =
      calculateCheckDigitUnsafe(tempCountryCode, tempSecurityIdentifier)

    if (tempCheckDigit != correctCheckDigit)
      throw new IllegalArgumentException(
        s"Check digit '$checkDigit' is not correct for country code '$countryCode' and security identifier '$securityIdentifier'. It should be '$correctCheckDigit'"
      )

    new ISIN(s"$tempCountryCode$tempSecurityIdentifier$tempCheckDigit")
  }

  /** Create an ISIN from a country code and security identifier, computing the correct check digit automatically.
    */
  def apply(countryCode: String, securityIdentifier: String): ISIN = {
    val tempCountryCode = countryCode.trim.toUpperCase
    val tempSecurityIdentifier = securityIdentifier.trim.toUpperCase

    if (!isValidCountryCodeFormatStrict(tempCountryCode))
      throw new IllegalArgumentException(
        s"Format of country code '$countryCode' is not valid"
      )

    if (!isValidSecurityIdentifierFormatStrict(tempSecurityIdentifier))
      throw new IllegalArgumentException(
        s"Format of security identifier '$securityIdentifier' is not valid"
      )

    val correctCheckDigit =
      calculateCheckDigitUnsafe(tempCountryCode, tempSecurityIdentifier)

    new ISIN(s"$tempCountryCode$tempSecurityIdentifier$correctCheckDigit")
  }

  def apply(value: String): ISIN = {
    val temp = value.trim.toUpperCase

    temp match {
      case isinFormat(countryCode, securityIdentifier, checkDigit) =>
        apply(countryCode, securityIdentifier, checkDigit)
      case _ =>
        throw new IllegalArgumentException(
          s"Input string is not in valid ISIN format: '$value'"
        )
    }
  }

}
