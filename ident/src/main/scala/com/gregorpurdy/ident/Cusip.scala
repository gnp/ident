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
    case c if c == 'Z'                 => false
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

object Cusip extends CusipVersionSpecific {

  val baseFormat: Regex = "[A-Z0-9]{6}".r
  val issueFormat: Regex = "[A-Z0-9]{2}".r
  val checkDigitFormat: Regex = "[0-9]".r
  val cusipFormat: Regex = "([A-Z0-9*@#]{6})([A-Z0-9*@#]{2})([0-9])".r

  def calculateCheckDigit(
      base: String,
      issue: String
  ): String = {
    val tempBase = normalize(base)
    val tempIssue = normalize(issue)

    if (!isValidBaseFormatStrict(tempBase))
      throw new IllegalArgumentException(
        s"Format of base code '$base' is not valid"
      )

    if (!isValidIssueFormatStrict(tempIssue))
      throw new IllegalArgumentException(
        s"Format of issue '$issue' is not valid"
      )

    calculateCheckDigitInternal(base, issue)
  }

  /** This method is used internally when the base and issue have already been validated to be the right format.
    */
  private def calculateCheckDigitInternal(
      base: String,
      issue: String
  ): String =
    Modulus10DoubleAddDouble.CusipVariant.calculate(s"$base$issue")

  def isValidBaseFormatStrict(string: String): Boolean =
    baseFormat.matches(string)

  def isValidBaseFormatLoose(string: String): Boolean =
    baseFormat.matches(normalize(string))

  def isValidIssueFormatStrict(string: String): Boolean =
    issueFormat.matches(string)

  def isValidIssueFormatLoose(string: String): Boolean =
    issueFormat.matches(normalize(string))

  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  def isValidCheckDigitFormatLoose(string: String): Boolean =
    checkDigitFormat.matches(normalize(string))

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 9 and each component is the right mix of letters, digits and/or special characters. It does not validate the check
    * digit.
    *
    * [[fromString]] is more permissive, because it will trim leading and/or trailing whitespace and convert to
    * uppercase before validating the CUSIP.
    */
  def isValidFormatStrict(string: String): Boolean =
    cusipFormat.matches(string)

  /** This returns true if the input String would be allowed as an argument to [[fromString]]. It does not validate the
    * check digit.
    */
  def isValidFormat(string: String): Boolean =
    cusipFormat.matches(normalize(string))

  def fromParts(
      base: String,
      issue: String,
      checkDigit: String
  ): Either[String, Cusip] = {
    val tempBase = normalize(base)
    val tempIssue = normalize(issue)
    val tempCheckDigit = normalize(checkDigit)

    if (!isValidBaseFormatStrict(tempBase))
      Left(s"Format of base '$base' is not valid")
    else if (!isValidIssueFormatStrict(tempIssue))
      Left(s"Format of issue '$issue' is not valid")
    else if (!isValidCheckDigitFormatStrict(tempCheckDigit))
      Left(s"Format of check digit '$checkDigit' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitInternal(base, issue)

      if (tempCheckDigit != correctCheckDigit)
        Left(
          s"Check digit '$checkDigit' is not correct for base '$base' and issue '$issue'. It should be '$correctCheckDigit'"
        )
      else
        Right(new Cusip(s"$base$issue$tempCheckDigit"))
    }
  }

  /** Create a CUSIP from a base and issue, computing the correct check digit automatically.
    */
  def fromPartsCalcCheckDigit(base: String, issue: String): Either[String, Cusip] = {
    val tempBase = normalize(base)
    val tempIssue = normalize(issue)

    if (!isValidBaseFormatStrict(tempBase))
      Left(s"Format of base '$base' is not valid")
    else if (!isValidIssueFormatStrict(tempIssue))
      Left(s"Format of issue '$issue' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitInternal(base, issue)
      Right(new Cusip(s"$base$issue$correctCheckDigit"))
    }
  }

  def fromString(value: String): Either[String, Cusip] =
    normalize(value) match {
      case cusipFormat(base, issue, checkDigit) =>
        fromParts(base, issue, checkDigit)
      case _ =>
        Left(s"Input string is not in valid CUSIP format: '$value'")
    }

}
