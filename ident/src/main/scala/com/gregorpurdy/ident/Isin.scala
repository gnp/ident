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

import com.gregorpurdy.ccs.Modulus10DoubleAddDouble

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
final case class Isin private (value: String) {
  def checkDigit: String = value.substring(11, 12)
  def countryCode: String = value.substring(0, 2)
  def securityIdentifier: String = value.substring(2, 11)
  override def toString(): String = value
  def toStringTagged: String = s"isin:$value"
}

object Isin extends IsinVersionSpecific {

  val countryCodeFormat: Regex = "[A-Z]{2}".r
  val securityIdentifierFormat: Regex = "[A-Z0-9]{9}".r
  val checkDigitFormat: Regex = "[0-9]".r
  val isinFormat: Regex = "([A-Z]{2})([A-Z0-9]{9})([0-9])".r

  /** Calculate the Check Digit for a given Country Code and Security Identifier.
    *
    * @param countryCode
    *   A two-letter ISO Country Code.
    * @param securityIdentifier
    *   A nine-character alphanumeric identifier
    * @return
    *   a `Right` value containing the Check Digit (a one-character String), or a `Left` value containing an error
    *   message
    */
  def calculateCheckDigit(
      countryCode: String,
      securityIdentifier: String
  ): Either[String, String] = {
    val tempCountryCode = normalize(countryCode)
    val tempSecurityIdentifier = normalize(securityIdentifier)

    if (!isValidCountryCodeFormatStrict(tempCountryCode))
      Left(s"Format of country code '$countryCode' is not valid")
    else if (!isValidSecurityIdentifierFormatStrict(tempSecurityIdentifier))
      Left(s"Format of security identifier '$securityIdentifier' is not valid")
    else
      Right(calculateCheckDigitInternal(tempCountryCode, tempSecurityIdentifier))
  }

  /** This method is used internally when the `countryCode` and `securityIdentifier` have already been validated to be
    * the right format.
    *
    * @param countryCode
    *   A two-letter ISO Country Code.
    * @param securityIdentifier
    *   A nine-character alphanumeric identifier
    * @return
    *   the Check Digit (a one-character String)
    */
  private def calculateCheckDigitInternal(
      countryCode: String,
      securityIdentifier: String
  ): String =
    Modulus10DoubleAddDouble.IsinVariant.calculate(s"$countryCode$securityIdentifier")

  def fromParts(
      countryCode: String,
      securityIdentifier: String,
      checkDigit: String
  ): Either[String, Isin] = {
    val cc = normalize(countryCode)
    val id = normalize(securityIdentifier)
    val cd = normalize(checkDigit)

    if (!isValidCountryCodeFormatStrict(cc))
      Left(s"Format of country code '$countryCode' is not valid")
    else if (!isValidSecurityIdentifierFormatStrict(id))
      Left(s"Format of security identifier '$securityIdentifier' is not valid")
    else if (!isValidCheckDigitFormatStrict(cd))
      Left(s"Format of check digit '$checkDigit' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitInternal(cc, id)

      if (cd != correctCheckDigit)
        Left(
          s"Check digit '$checkDigit' is not correct for country code '$countryCode' and security identifier '$securityIdentifier'. It should be '$correctCheckDigit'"
        )
      else
        Right(new Isin(s"$cc$id$cd"))
    }
  }

  /** Create an ISIN from a country code and security identifier, computing the correct check digit automatically.
    */
  def fromPartsCalcCheckDigit(countryCode: String, securityIdentifier: String): Either[String, Isin] = {
    val cc = normalize(countryCode)
    val id = normalize(securityIdentifier)

    if (!isValidCountryCodeFormatStrict(cc))
      Left(s"Format of country code '$countryCode' is not valid")
    else if (!isValidSecurityIdentifierFormatStrict(id))
      Left(s"Format of security identifier '$securityIdentifier' is not valid")
    else {
      val cd = calculateCheckDigitInternal(cc, id)

      Right(new Isin(s"$cc$id$cd"))
    }
  }

  def fromString(value: String): Either[String, Isin] =
    normalize(value) match {
      case isinFormat(countryCode, securityIdentifier, checkDigit) =>
        fromParts(countryCode, securityIdentifier, checkDigit)
      case _ =>
        Left(s"Input string is not in valid ISIN format: '$value'")
    }

  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  def isValidCheckDigitFormatLoose(string: String): Boolean =
    checkDigitFormat.matches(normalize(string))

  def isValidCountryCodeFormatStrict(string: String): Boolean =
    countryCodeFormat.matches(string)

  def isValidCountryCodeFormatLoose(string: String): Boolean =
    countryCodeFormat.matches(normalize(string))

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 11 and each component is the right mix of letters and/or digits. It does not validate the check digit.
    *
    * [[fromString]] is more permissive, because it will trim leading and/or trailing whitespace and convert to
    * uppercase before validating the ISIN.
    */
  def isValidFormatStrict(string: String): Boolean =
    isinFormat.matches(string)

  /** This returns true if the input String would be allowed as an argument to [[fromString]]. It does not validate the
    * check digit.
    */
  def isValidFormat(string: String): Boolean =
    isinFormat.matches(normalize(string))

  def isValidSecurityIdentifierFormatStrict(string: String): Boolean =
    securityIdentifierFormat.matches(string)

  def isValidSecurityIdentifierFormatLoose(string: String): Boolean =
    securityIdentifierFormat.matches(normalize(string))

}
