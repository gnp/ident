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

/** A type for working with validated Committee on Uniform Security Identification Procedures (CUSIP) identifiers as
  * defined in [ANSI X9.6-2020 Financial Services - Committee on Uniform Security Identification Procedures Securities
  * Identification CUSIP](https://webstore.ansi.org/standards/ascx9/ansix92020) ("The Standard").
  *
  * A CUSIP "number" (so-called by The Standard because originally they were composed only of decimal digits, but now
  * they can also use letters) is comprised of 9 ASCII characters with the following parts, in order (Section 3.1 "CUSIP
  * number length" of the standard):
  *
  *   1. A six-character uppercase alphanumeric _Issuer Number_.
  *   1. A two-character uppercase alphanumeric _Issue Number_.
  *   1. A single decimal digit representing the _Check Digit_ computed using what The Standard calls the "modulus 10
  *      'double-add-double' technique".
  *
  * @see
  *   https://en.wikipedia.org/wiki/CUSIP
  *
  * @see
  *   https://www.cusip.com/identifiers.html?section=CUSIP
  */
final case class Cusip private (value: String) {

  /** @return just the _Check Digit_ portion of the CUSIP (one character). */
  def checkDigit: String = value.substring(8, 9)

  /** @return
    *   `Some(c)` containing the first character of the CUSIP if it is actually a CUSIP International Numbering System
    *   (CINS) identifier, `None` otherwise.
    */
  def cinsCountryCode: Option[Char] = value(0) match {
    case c if (c >= '0') && (c <= '9') => None
    case c if (c >= 'A') && (c <= 'Z') => Some(c)
    case _ =>
      throw new IllegalStateException(
        s"It should not be possible to have the first character of a CUSIP other than uppercase ASCII alphanumeric, but it is '${value(0)}' in \"$value\""
      )
  }

  /** @return true if [[issueNumber]] is reserved for private use. */
  def hasPrivateIssue: Boolean = {
    val temp = issuerNumber
    val nineInTensPlace = temp(6) == '9'
    val digitInOnesPlace = Character.isDigit(temp(7))
    val letterInOnesPlace = (temp(7) >= 'A') && (temp(7) <= 'Y')
    nineInTensPlace && (digitInOnesPlace || letterInOnesPlace)
  }

  /** @return true if [[issuerNumber]] is reserved for private use. */
  def hasPrivateIssuer: Boolean = {
    val temp = issuerNumber
    if (temp(3) == '9' && temp(4) == '9') {
      // "???99?"
      true
    } else if (
      temp(0) == '9' && temp(1) == '9' && Character.isDigit(temp(2)) && Character
        .isDigit(temp(3)) && Character.isDigit(temp(4))
    ) {
      // "99000?" to "99999?"
      true
    } else {
      false
    }
  }

  /** @return
    *   `true` if this CUSIP identifier is actually a CUSIP International Numbering System (CINS) identifier, `false`
    *   otherwise (i.e., that it has a letter as the first character of `issuerNumber`).
    *
    * @see
    *   [[isCinsBase]] and [[isCinsExtended]].
    */
  def isCins: Boolean = value(0) match {
    case c if (c >= '0') && (c <= '9') => false
    case c if (c >= 'A') && (c <= 'Z') => true
    case _ =>
      throw new IllegalStateException(
        s"It should not be possible to have the first character of a CUSIP other than uppercase ASCII alphanumeric, but it is '${value(0)}' in \"$value\""
      )
  }

  /** @return
    *   `true` if this CUSIP identifier is actually a CUSIP International Numbering System (CINS) identifier (with the
    *   further restriction that it *does not* use 'I', 'O' or 'Z' as its country code), false otherwise.
    *
    * @see
    *   [[isCins]] and [[isCinsExtended]].
    */
  def isCinsBase: Boolean = value(0) match {
    case c if (c >= '0') && (c <= '9') => false
    case c if (c >= 'A') && (c <= 'H') => true
    case c if c == 'I'                 => false
    case c if (c >= 'J') && (c <= 'N') => true
    case c if c == 'O'                 => false
    case c if (c >= 'P') && (c <= 'Y') => true
    case c if c == 'Z'                 => false
    case _ =>
      throw new IllegalStateException(
        s"It should not be possible to have the first character of a CUSIP other than uppercase ASCII alphanumeric, but it is '${value(0)}' in \"$value\""
      )
  }

  /** @return
    *   `true` if this CUSIP identifier is actually a CUSIP International Numbering System (CINS) identifier (with the
    *   restriction that it *does* use 'I', 'O' or 'Z' as its country code), false otherwise.
    *
    * @see
    *   [[isCins]] and [[isCinsBase]].
    */
  def isCinsExtended: Boolean = value(0) match {
    case c if (c >= '0') && (c <= '9') => false
    case c if (c >= 'A') && (c <= 'H') => false
    case c if c == 'I'                 => true
    case c if (c >= 'J') && (c <= 'N') => false
    case c if c == 'O'                 => true
    case c if (c >= 'P') && (c <= 'Y') => false
    case c if c == 'Z'                 => true
    case _ =>
      throw new IllegalStateException(
        s"It should not be possible to have the first character of a CUSIP other than uppercase ASCII alphanumeric, but it is '${value(0)}' in \"$value\""
      )
  }

  /** @return
    *   `true` if the CUSIP is reserved for private use (i.e., either [[hasPrivateIssuer]] or [[hasPrivateIssue]] are
    *   `true`.
    */
  def isPrivateUse: Boolean = hasPrivateIssuer || hasPrivateIssue

  /** @return just the _Issue Number_ portion of the CUSIP (two characters). */
  def issueNumber: String = value.substring(6, 8)

  /** @return just the _Issuer Number_ portion of the CUSIP (six characters). */
  def issuerNumber: String = value.substring(0, 6)

  /** @return the "payload" -- everything except the _Check Digit_. */
  def payload: String = value.substring(0, 8)

  /** @return [[value]] since it is already a `String`. */
  override def toString: String = value

  /** @return [[value]] prefixed by "`cusip:`". */
  def toStringTagged: String = s"cusip:$value"

}

sealed trait CusipError
object CusipError {
  case class IncorrectCheckDigitValue(was: String, expected: String, issuer: String, issue: String) extends CusipError {
    override def toString(): String =
      s"Check Digit '$was' is not correct for CUSIP Issuer '$issuer' and Issue '$issue'. It should be '$expected'."
  }
  case class InvalidCheckDigitFormat(was: String) extends CusipError {
    override def toString(): String = s"Format of Check Digit '$was' is not valid for CUSIP."
  }
  case class InvalidCusipFormat(was: String) extends CusipError {
    override def toString(): String = s"Format of identifier '$was' is not valid for CUSIP."
  }
  case class InvalidIssueFormat(was: String) extends CusipError {
    override def toString(): String = s"Format of Issue '$was' is not valid for CUSIP."
  }
  case class InvalidIssuerFormat(was: String) extends CusipError {
    override def toString(): String = s"Format of Issuer '$was' is not valid for CUSIP."
  }
  case class InvalidPayloadFormat(was: String) extends CusipError {
    override def toString(): String = s"Format of payload '$was' is not valid for CUSIP."
  }
}

object Cusip extends CusipVersionSpecific {
  import CusipError.*

  val issuerFormat: Regex = "([A-Z0-9]{6})".r
  val issueFormat: Regex = "([A-Z0-9]{2})".r
  val payloadFormat: Regex = "([A-Z0-9]{8})".r
  val checkDigitFormat: Regex = "([0-9])".r
  val cusipFormat: Regex = "([A-Z0-9]{6})([A-Z0-9]{2})([0-9])".r
  val cusipFormatFull: Regex = "([A-Z0-9]{8}[0-9])".r

  def calculateCheckDigitFromPayloadParts(
      issuer: String,
      issue: String
  ): Either[CusipError, String] = for {
    issuer <- validateIssuerFormat(issuer)
    issue <- validateIssueFormat(issue)
  } yield calculateCheckDigitFromPayloadPartsInternal(issuer, issue)

  /** This method is used internally when the base and issue have already been validated to be the right format.
    */
  private[ident] def calculateCheckDigitFromPayloadPartsInternal(
      issuer: String,
      issue: String
  ): String =
    calculateCheckDigitFromPayloadInternal(s"$issuer$issue")

  def calculateCheckDigitFromPayloadPartsStrict(
      issuer: String,
      issue: String
  ): Either[CusipError, String] = for {
    issuer <- validateIssuerFormatStrict(issuer)
    issue <- validateIssueFormatStrict(issue)
  } yield calculateCheckDigitFromPayloadPartsInternal(issuer, issue)

  def calculateCheckDigitFromPayload(
      payload: String
  ): Either[CusipError, String] = for {
    payload <- validatePayloadFormat(payload)
  } yield calculateCheckDigitFromPayloadInternal(payload)

  /** This method is used internally when the payload has already been validated to be the right format.
    */
  private[ident] def calculateCheckDigitFromPayloadInternal(
      payload: String
  ): String =
    Modulus10DoubleAddDouble.CusipVariant.calculate(payload)

  def calculateCheckDigitFromPayloadStrict(
      payload: String
  ): Either[CusipError, String] = for {
    payload <- validatePayloadFormatStrict(payload)
  } yield calculateCheckDigitFromPayloadInternal(payload)

  /** Construct a [[Cusip]] from an _Issuer_, an _Issue_, and a _Check Digit_, each of which is first normalized
    * (trimmed and converted to upper case) before being checked for proper format and used.
    */
  def fromParts(
      issuer: String,
      issue: String,
      checkDigit: String
  ): Either[CusipError, Cusip] = for {
    issuer <- validateIssuerFormat(issuer)
    issue <- validateIssueFormat(issue)
    checkDigit <- validateCheckDigitFormat(checkDigit)
    cusip <- fromPartsInternal(issuer, issue, checkDigit)
  } yield cusip

  /** Construct a [[Cusip]] from an _Issuer_, an _Issue_, and a _Check Digit_, each of which is required to be (and is
    * **assumed** to be) in the proper format.
    *
    * A final validation is peformed to ensure the _Check Digit_ is correct before constructing the [[Cusip]] instance.
    */
  private[ident] def fromPartsInternal(
      issuer: String,
      issue: String,
      checkDigit: String
  ): Either[CusipError, Cusip] = for {
    _ <- validateCheckDigitForPartsInternal(issuer, issue, checkDigit)
  } yield new Cusip(s"$issuer$issue$checkDigit")

  /** Construct a [[Cusip]] from an _Issuer_, an _Issue_, and a _Check Digit_, each of which is required to be (and is
    * **checked** to be) in the proper format.
    */
  def fromPartsStrict(
      issuer: String,
      issue: String,
      checkDigit: String
  ): Either[CusipError, Cusip] = for {
    issuer <- validateIssuerFormatStrict(issuer)
    issue <- validateIssueFormatStrict(issue)
    checkDigit <- validateCheckDigitFormatStrict(checkDigit)
    cusip <- fromPartsInternal(issuer, issue, checkDigit)
  } yield cusip

  /** Create a [[Cusip]] from a payload that combines an _Issuer_ and an _Issue_, computing the correct _Check Digit_
    * automatically.
    */
  def fromPayload(payload: String): Either[CusipError, Cusip] = for {
    payload <- validatePayloadFormat(payload)
  } yield fromPayloadInternal(payload)

  private[ident] def fromPayloadInternal(payload: String): Cusip = {
    val checkDigit = calculateCheckDigitFromPayloadInternal(payload)
    new Cusip(s"$payload$checkDigit")
  }

  /** Create a [[Cusip]] from an _Issuer_ and an _Issue_, computing the correct _Check Digit_ automatically.
    */
  def fromPayloadParts(issuer: String, issue: String): Either[CusipError, Cusip] = for {
    issuer <- validateIssuerFormat(issuer)
    issue <- validateIssueFormat(issue)
  } yield fromPayloadPartsInternal(issuer, issue)

  private[ident] def fromPayloadPartsInternal(
      issuer: String,
      issue: String
  ): Cusip = {
    val checkDigit = calculateCheckDigitFromPayloadPartsInternal(issuer, issue)
    new Cusip(s"$issuer$issue$checkDigit")
  }

  def fromPayloadPartsStrict(issuer: String, issue: String): Either[CusipError, Cusip] = for {
    issuer <- validateIssuerFormatStrict(issuer)
    issue <- validateIssueFormatStrict(issue)
  } yield fromPayloadPartsInternal(issuer, issue)

  /** Create a [[Cusip]] from a payload that combines an _Issuer_ and an _Issue_, computing the correct _Check Digit_
    * automatically.
    */
  def fromPayloadStrict(payload: String): Either[CusipError, Cusip] = for {
    payload <- validatePayloadFormatStrict(payload)
  } yield fromPayloadInternal(payload)

  def fromString(value: String): Either[CusipError, Cusip] =
    normalize(value) match {
      case cusipFormat(issuer, issue, checkDigit) =>
        fromPartsInternal(issuer, issue, checkDigit)
      case _ =>
        Left(InvalidCusipFormat(value))
    }

  def fromStringStrict(value: String): Either[CusipError, Cusip] =
    value match {
      case cusipFormat(issuer, issue, checkDigit) =>
        fromPartsInternal(issuer, issue, checkDigit)
      case _ =>
        Left(InvalidCusipFormat(value))
    }

  def isValidCheckDigitFormat(string: String): Boolean =
    checkDigitFormat.matches(normalize(string))

  /** This returns `true` if the input String would be allowed as the _Check Digit_ argument to [[fromPartsStrict]].
    *
    * This will only return true if the input String has no whitespace, the length is 1 and the character is a decimal
    * digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP _Issue_ format, `false` otherwise.
    */
  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  /** This returns `true` if the input String would be allowed as the argument to [[fromString]]. It **does not**
    * validate the _Check Digit_.
    *
    * This will still return true if the input String has leading or trailing whitespace, or lowercase letters, as long
    * as the length is 9 and each component is the right mix of letters and/or decimal digits. It **does not** validate
    * the _Check Digit_ value.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP format, ignoring leading or trailing whitespace and allowing lowercase
    *   letters (whether or not the _Check Digit_ is correct), `false` otherwise.
    */
  def isValidFormat(string: String): Boolean =
    cusipFormat.matches(normalize(string))

  /** This returns `true` if the input String would be allowed as the argument to [[fromStringStrict]]. It **does not**
    * validate the _Check Digit_.
    *
    * This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 9 and each component is the right mix of letters and/or decimal digits. It **does not** validate the _Check Digit_
    * value.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP format (whether or not the _Check Digit_ is correct), `false` otherwise.
    */
  def isValidFormatStrict(string: String): Boolean =
    cusipFormat.matches(string)

  /** This returns `true` if the input String would be allowed as the _Issue_ argument to [[fromParts]] or
    * [[fromPayloadParts]].
    *
    * This will still return true if the input String has leading or trailing whitespace, or lowercase letters, as long
    * as the length is 2 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP _Issue_ format, ignoring leading or trailing whitespace and allowing
    *   lowercase letters, `false` otherwise.
    */
  def isValidIssueFormat(string: String): Boolean =
    issueFormat.matches(normalize(string))

  /** This returns `true` if the input String would be allowed as the _Issue_ argument to [[fromPartsStrict]] or
    * [[fromPayloadPartsStrict]].
    *
    * This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 2 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP _Issue_ format, `false` otherwise.
    */
  def isValidIssueFormatStrict(string: String): Boolean =
    issueFormat.matches(string)

  /** This returns `true` if the input String would be allowed as the _Issuer_ argument to [[fromParts]] or
    * [[fromPayloadParts]].
    *
    * This will still return true if the input String has leading or trailing whitespace, or lowercase letters, as long
    * as the length is 6 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP _Issuer_ format, ignoring leading or trailing whitespace and allowing
    *   lowercase letters, `false` otherwise.
    */
  def isValidIssuerFormat(string: String): Boolean =
    issuerFormat.matches(normalize(string))

  /** This returns `true` if the input String would be allowed as the _Issuer_ argument to [[fromPartsStrict]] or
    * [[fromPayloadPartsStrict]].
    *
    * This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 6 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP _Issuer_ format, `false` otherwise.
    */
  def isValidIssuerFormatStrict(string: String): Boolean =
    issuerFormat.matches(string)

  /** This returns `true` if the input String would be allowed as the argument to [[fromPayload]].
    *
    * This will still return true if the input String has leading or trailing whitespace, or lowercase letters, as long
    * as the length is 8 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP payload format, ignoring leading or trailing whitespace and allowing
    *   lowercase letters, `false` otherwise.
    */
  def isValidPayloadFormat(string: String): Boolean =
    payloadFormat.matches(normalize(string))

  /** This returns `true` if the input String would be allowed as the argument to [[fromPayloadStrict]].
    *
    * This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 8 and each character is a letter or decimal digit.
    *
    * @param string
    *   The value to be validated.
    * @return
    *   `true` if the string has valid CUSIP payload format, `false` otherwise.
    */
  def isValidPayloadFormatStrict(string: String): Boolean =
    payloadFormat.matches(string)

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
  def validateCheckDigitFormat(string: String): Either[CusipError, String] =
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
  def validateCheckDigitFormatStrict(string: String): Either[CusipError, String] =
    string match {
      case checkDigitFormat(s) => Right(s)
      case _                   => Left(InvalidCheckDigitFormat(string))
    }

  /** Private internal function to validate that `checkDigit` is correct for `issuer` and `issue`.
    *
    * @param issuer
    *   An _Issuer_ identifier, **already validated** to be in the correct format.
    * @param issue
    *   An _Issue_ identifier, **already validated** to be in the correct format.
    * @param checkDigit
    *   A _Check Digit_, **already validated** to be in the correct format.
    * @return
    *   a `Right` value containing the correct _Check Digit_, or a `Left` value indicating the error.
    */
  private[ident] def validateCheckDigitForPartsInternal(
      issuer: String,
      issue: String,
      checkDigit: String
  ): Either[CusipError, String] =
    calculateCheckDigitFromPayloadPartsInternal(issuer, issue) match {
      case s if s == checkDigit => Right(checkDigit)
      case expected             => Left(IncorrectCheckDigitValue(checkDigit, expected, issuer, issue))
    }

  def validateFormat(string: String): Either[CusipError, String] =
    normalize(string) match {
      case cusipFormatFull(s) => Right(s)
      case _                  => Left(InvalidCusipFormat(string))
    }

  def validateFormatStrict(string: String): Either[CusipError, String] =
    string match {
      case cusipFormatFull(s) => Right(s)
      case _                  => Left(InvalidCusipFormat(string))
    }

  def validateIssueFormat(string: String): Either[CusipError, String] =
    normalize(string) match {
      case issueFormat(s) => Right(s)
      case _              => Left(InvalidIssueFormat(string))
    }

  def validateIssueFormatStrict(string: String): Either[CusipError, String] =
    string match {
      case issueFormat(s) => Right(s)
      case _              => Left(InvalidIssueFormat(string))
    }

  def validateIssuerFormat(string: String): Either[CusipError, String] =
    normalize(string) match {
      case issuerFormat(s) => Right(s)
      case _               => Left(InvalidIssuerFormat(string))
    }

  def validateIssuerFormatStrict(string: String): Either[CusipError, String] =
    string match {
      case issuerFormat(s) => Right(s)
      case _               => Left(InvalidIssuerFormat(string))
    }

  def validatePayloadFormat(string: String): Either[CusipError, String] =
    normalize(string) match {
      case payloadFormat(s) => Right(s)
      case _                => Left(InvalidPayloadFormat(string))
    }

  def validatePayloadFormatStrict(string: String): Either[CusipError, String] =
    string match {
      case payloadFormat(s) => Right(s)
      case _                => Left(InvalidPayloadFormat(string))
    }

}
