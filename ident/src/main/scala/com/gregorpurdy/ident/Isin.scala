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

import com.gregorpurdy.ccs.Modulus10DoubleAddDouble

import scala.util.CommandLineParser
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

sealed trait IsinError
object IsinError {
  case class IncorrectCheckDigitValue(was: String, expected: String, countryCode: String, securityIdentifier: String)
      extends IsinError {
    override def toString(): String =
      s"Check Digit '$was' is not correct for ISIN Country Code '$countryCode' and Security Identifier '$securityIdentifier'. It should be '$expected'."
  }
  case class InvalidCheckDigitFormat(was: String) extends IsinError {
    override def toString(): String = s"Format of Check Digit '$was' is not valid for ISIN."
  }
  case class InvalidCountryCodeFormat(was: String) extends IsinError {
    override def toString(): String = s"Format of Country Code '$was' is not valid for ISIN."
  }
  case class InvalidIsinFormat(was: String) extends IsinError {
    override def toString(): String = s"Format of identifier '$was' is not valid for ISIN."
  }
  case class InvalidPayloadFormat(was: String) extends IsinError {
    override def toString(): String = s"Format of payload '$was' is not valid for ISIN."
  }
  case class InvalidSecurityIdentifierFormat(was: String) extends IsinError {
    override def toString(): String = s"Format of Security Identifier '$was' is not valid for ISIN."
  }
}

object Isin {
  import IsinError.*

  val countryCodeFormat: Regex = "([A-Z]{2})".r
  val securityIdentifierFormat: Regex = "([A-Z0-9]{9})".r
  val payloadFormat: Regex = "([A-Z]{2}[A-Z0-9]{9})".r
  val checkDigitFormat: Regex = "([0-9])".r
  val isinFormat: Regex = "([A-Z]{2})([A-Z0-9]{9})([0-9])".r
  val isinFormatFull: Regex = "([A-Z]{2}[A-Z0-9]{9}[0-9])".r

  given Ordering[Isin] = Ordering.by(_.value)

  object IsinCommandLineParserFromString extends CommandLineParser.FromString[Isin] {
    def fromString(s: String): Isin = Isin.fromString(s) match {
      case Left(s)      => throw new IllegalArgumentException(s.toString)
      case Right(ident) => ident
    }
    override def fromStringOption(s: String): Option[Isin] = Isin.fromString(s).toOption
  }

  given CommandLineParser.FromString[Isin] = IsinCommandLineParserFromString

  def calculateCheckDigitFromPayload(
      payload: String
  ): Either[IsinError, String] = for {
    payload <- validatePayloadFormat(payload)
  } yield calculateCheckDigitFromPayloadInternal(payload)

  /** This method is used internally when the payload has already been validated to be the right format.
    */
  private[ident] def calculateCheckDigitFromPayloadInternal(
      payload: String
  ): String =
    Modulus10DoubleAddDouble.IsinVariant.calculate(payload)

  def calculateCheckDigitFromPayloadStrict(
      payload: String
  ): Either[IsinError, String] = for {
    payload <- validatePayloadFormatStrict(payload)
  } yield calculateCheckDigitFromPayloadInternal(payload)

  /** Calculate the Check Digit for a given Country Code and Security Identifier.
    *
    * @param countryCode
    *   A two-letter ISO Country Code. It is normalized before use.
    * @param securityIdentifier
    *   A nine-character alphanumeric identifier. It is normalized before use.
    * @return
    *   a `Right` value containing the Check Digit (a one-character String), or a `Left` value containing an IsinError.
    */
  def calculateCheckDigitFromPayloadParts(
      countryCode: String,
      securityIdentifier: String
  ): Either[IsinError, String] = for {
    countryCode <- validateCountryCodeFormat(countryCode)
    securityIdentifier <- validateSecurityIdentifierFormat(securityIdentifier)
  } yield calculateCheckDigitFromPayloadPartsInternal(countryCode, securityIdentifier)

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
  private[ident] def calculateCheckDigitFromPayloadPartsInternal(
      countryCode: String,
      securityIdentifier: String
  ): String =
    calculateCheckDigitFromPayloadInternal(s"$countryCode$securityIdentifier")

  /** Calculate the Check Digit for a given Country Code and Security Identifier.
    *
    * @param countryCode
    *   A two-letter ISO Country Code. It is **not** normalized before use.
    * @param securityIdentifier
    *   A nine-character alphanumeric identifier. It is **not** normalized before use.
    * @return
    *   a `Right` value containing the Check Digit (a one-character String), or a `Left` value containing an IsinError.
    */
  def calculateCheckDigitFromPayloadPartsStrict(
      countryCode: String,
      securityIdentifier: String
  ): Either[IsinError, String] = for {
    countryCode <- validateCountryCodeFormatStrict(countryCode)
    securityIdentifier <- validateSecurityIdentifierFormatStrict(securityIdentifier)
  } yield calculateCheckDigitFromPayloadPartsInternal(countryCode, securityIdentifier)

  /** Construct an [[Isin]] from a _Country Code_, a _Security Identifier_, and a _Check Digit_, each of which is first
    * normalized (trimmed and converted to upper case) before being checked for proper format and used.
    */
  def fromParts(
      countryCode: String,
      securityIdentifier: String,
      checkDigit: String
  ): Either[IsinError, Isin] = for {
    countryCode <- validateCountryCodeFormat(countryCode)
    securityIdentifier <- validateSecurityIdentifierFormat(securityIdentifier)
    checkDigit <- validateCheckDigitFormat(checkDigit)
    isin <- fromPartsInternal(countryCode, securityIdentifier, checkDigit)
  } yield isin

  /** Construct an [[Isin]] from a _Country Code_, a _Security Identifier_, and a _Check Digit_, each of which is
    * required to be (and is **assumed** to be) in the proper format.
    *
    * A final validation is peformed to ensure the _Check Digit_ is correct before constructing the [[Isin]] instance.
    */
  private[ident] def fromPartsInternal(
      countryCode: String,
      securityIdentifier: String,
      checkDigit: String
  ): Either[IsinError, Isin] = for {
    _ <- validateCheckDigitForPartsInternal(countryCode, securityIdentifier, checkDigit)
  } yield new Isin(s"$countryCode$securityIdentifier$checkDigit")

  /** Construct an [[Isin]] from a _Country Code_, a _Security Identifier_, and a _Check Digit_, each of which is
    * required to be (and is **checked** to be) in the proper format.
    */
  def fromPartsStrict(
      countryCode: String,
      securityIdentifier: String,
      checkDigit: String
  ): Either[IsinError, Isin] = for {
    countryCode <- validateCountryCodeFormatStrict(countryCode)
    securityIdentifier <- validateSecurityIdentifierFormatStrict(securityIdentifier)
    checkDigit <- validateCheckDigitFormatStrict(checkDigit)
    isin <- fromPartsInternal(countryCode, securityIdentifier, checkDigit)
  } yield isin

  /** Create an [[Isin]] from a payload that combines a _Country Code_ and a _Security Identifier_, computing the
    * correct _Check Digit_ automatically.
    */
  def fromPayload(payload: String): Either[IsinError, Isin] = for {
    payload <- validatePayloadFormat(payload)
  } yield fromPayloadInternal(payload)

  private[ident] def fromPayloadInternal(payload: String): Isin = {
    val checkDigit = calculateCheckDigitFromPayloadInternal(payload)
    new Isin(s"$payload$checkDigit")
  }

  /** Create an [[Isin]] from a _Country Code_ and a _Security Identifier_, computing the correct _Check Digit_
    * automatically.
    */
  def fromPayloadParts(countryCode: String, securityIdentifier: String): Either[IsinError, Isin] = for {
    countryCode <- validateCountryCodeFormat(countryCode)
    securityIdentifier <- validateSecurityIdentifierFormat(securityIdentifier)
  } yield fromPayloadPartsInternal(countryCode, securityIdentifier)

  private[ident] def fromPayloadPartsInternal(
      countryCode: String,
      securityIdentifier: String
  ): Isin = {
    val checkDigit = calculateCheckDigitFromPayloadPartsInternal(countryCode, securityIdentifier)
    new Isin(s"$countryCode$securityIdentifier$checkDigit")
  }

  def fromPayloadPartsStrict(countryCode: String, securityIdentifier: String): Either[IsinError, Isin] = for {
    countryCode <- validateCountryCodeFormatStrict(countryCode)
    securityIdentifier <- validateSecurityIdentifierFormatStrict(securityIdentifier)
  } yield fromPayloadPartsInternal(countryCode, securityIdentifier)

  /** Create an [[Isin]] from a payload that combines a _Country Code_ and a _Security Identifier_, computing the
    * correct _Check Digit_ automatically.
    */
  def fromPayloadStrict(payload: String): Either[IsinError, Isin] = for {
    payload <- validatePayloadFormatStrict(payload)
  } yield fromPayloadInternal(payload)

  def fromString(value: String): Either[IsinError, Isin] =
    normalize(value) match {
      case isinFormat(countryCode, securityIdentifier, checkDigit) =>
        fromPartsInternal(countryCode, securityIdentifier, checkDigit)
      case _ =>
        Left(InvalidIsinFormat(value))
    }

  def fromStringStrict(value: String): Either[IsinError, Isin] =
    value match {
      case isinFormat(countryCode, securityIdentifier, checkDigit) =>
        fromPartsInternal(countryCode, securityIdentifier, checkDigit)
      case _ =>
        Left(InvalidIsinFormat(value))
    }

  def isValidCheckDigitFormat(string: String): Boolean =
    checkDigitFormat.matches(normalize(string))

  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  def isValidCountryCodeFormat(string: String): Boolean =
    countryCodeFormat.matches(normalize(string))

  def isValidCountryCodeFormatStrict(string: String): Boolean =
    countryCodeFormat.matches(string)

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

  /** This returns `true` if the input String would be allowed as the argument to [[fromPayload]].
    *
    * This will still return true if the input String has leading or trailing whitespace, or lowercase letters, as long
    * as the length is 11 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid ISIN payload format, ignoring leading or trailing whitespace and allowing
    *   lowercase letters, `false` otherwise.
    */
  def isValidPayloadFormat(string: String): Boolean =
    payloadFormat.matches(normalize(string))

  /** This returns `true` if the input String would be allowed as the argument to [[fromPayloadStrict]].
    *
    * This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 11 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid ISIN payload format, `false` otherwise.
    */
  def isValidPayloadFormatStrict(string: String): Boolean =
    payloadFormat.matches(string)

  def isValidSecurityIdentifierFormat(string: String): Boolean =
    securityIdentifierFormat.matches(normalize(string))

  def isValidSecurityIdentifierFormatStrict(string: String): Boolean =
    securityIdentifierFormat.matches(string)

  /** Validate that `string` is in the correct format for a _Check Digit_ -- after first normalizing.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   a `Right` value containing the _Check Digit_ in valid format, or a `Left` value indicating the error.
    * @see
    *   [[validateCheckDigitFormatStrict]]
    * @see
    *   [[normalize]]
    */
  def validateCheckDigitFormat(string: String): Either[IsinError, String] =
    normalize(string) match {
      case checkDigitFormat(s) => Right(s)
      case _                   => Left(InvalidCheckDigitFormat(string))
    }

  /** Validate that `string` is in the correct format for a _Check Digit_. This uses strict validation; the provided
    * value is not normalized first.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   a `Right` value containing the _Check Digit_ in valid format, or a `Left` value indicating the error.
    * @see
    *   [[validateCheckDigitFormat]]
    */
  def validateCheckDigitFormatStrict(string: String): Either[IsinError, String] =
    string match {
      case checkDigitFormat(s) => Right(s)
      case _                   => Left(InvalidCheckDigitFormat(string))
    }

  /** Private internal function to validate that `checkDigit` is correct for `issuer` and `issue`.
    *
    * @param countryCode
    *   A _Country Code_, **already validated** to be in the correct format.
    * @param securityIdentifier
    *   A _Security Identifier_, **already validated** to be in the correct format.
    * @param checkDigit
    *   A _Check Digit_, **already validated** to be in the correct format.
    * @return
    *   a `Right` value containing the correct _Check Digit_, or a `Left` value indicating the error.
    */
  private[ident] def validateCheckDigitForPartsInternal(
      countryCode: String,
      securityIdentifier: String,
      checkDigit: String
  ): Either[IsinError, String] =
    calculateCheckDigitFromPayloadPartsInternal(countryCode, securityIdentifier) match {
      case s if s == checkDigit => Right(checkDigit)
      case expected             => Left(IncorrectCheckDigitValue(checkDigit, expected, countryCode, securityIdentifier))
    }

  def validateCountryCodeFormat(string: String): Either[IsinError, String] =
    normalize(string) match {
      case countryCodeFormat(s) => Right(s)
      case _                    => Left(InvalidCountryCodeFormat(string))
    }

  def validateCountryCodeFormatStrict(string: String): Either[IsinError, String] =
    string match {
      case countryCodeFormat(s) => Right(s)
      case _                    => Left(InvalidCountryCodeFormat(string))
    }

  def validateFormat(string: String): Either[IsinError, String] =
    normalize(string) match {
      case isinFormatFull(s) => Right(s)
      case _                 => Left(InvalidIsinFormat(string))
    }

  def validateFormatStrict(string: String): Either[IsinError, String] =
    string match {
      case isinFormatFull(s) => Right(s)
      case _                 => Left(InvalidIsinFormat(string))
    }

  def validatePayloadFormat(string: String): Either[IsinError, String] =
    normalize(string) match {
      case payloadFormat(s) => Right(s)
      case _                => Left(InvalidPayloadFormat(string))
    }

  def validatePayloadFormatStrict(string: String): Either[IsinError, String] =
    string match {
      case payloadFormat(s) => Right(s)
      case _                => Left(InvalidPayloadFormat(string))
    }

  def validateSecurityIdentifierFormat(string: String): Either[IsinError, String] =
    normalize(string) match {
      case securityIdentifierFormat(s) => Right(s)
      case _                           => Left(InvalidSecurityIdentifierFormat(string))
    }

  def validateSecurityIdentifierFormatStrict(string: String): Either[IsinError, String] =
    string match {
      case securityIdentifierFormat(s) => Right(s)
      case _                           => Left(InvalidSecurityIdentifierFormat(string))
    }

}
