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

/** @see
  *   https://en.wikipedia.org/wiki/CUSIP
  *
  * @see
  *   https://www.cusip.com/identifiers.html?section=CUSIP
  */
final case class Cusip private (value: String) {
  def base: String = value.substring(0, 6)
  def issue: String = value.substring(6, 8)
  def checkDigit: String = value.substring(8, 9)
  override def toString: String = value
  def toStringTagged: String = s"cusip:$value"
}

object Cusip {

  implicit val ord: Ordering[Cusip] = Ordering.by(_.value)

  val baseFormat: Regex = "[A-Z0-9*@#]{6}".r
  val issueFormat: Regex = "[A-Z0-9*@#]{2}".r
  val checkDigitFormat: Regex = "[0-9]".r
  val cusipFormat: Regex = "([A-Z0-9*@#]{6})([A-Z0-9*@#]{2})([0-9])".r

  def calculateCheckDigit(
      base: String,
      issue: String
  ): String = {
    val tempBase = base.trim.toUpperCase
    val tempIssue = issue.trim.toUpperCase

    if (!isValidBaseFormatStrict(tempBase))
      throw new IllegalArgumentException(
        s"Format of base code '$base' is not valid"
      )

    if (!isValidIssueFormatStrict(tempIssue))
      throw new IllegalArgumentException(
        s"Format of issue '$issue' is not valid"
      )

    calculateCheckDigitUnsafe(base, issue)
  }

  /** This method is used internally when the base and issue have already been validated to be the right format.
    */
  private def calculateCheckDigitUnsafe(
      base: String,
      issue: String
  ): String = {
    val s = s"$base$issue"
    var sum: Int = 0
    for (i <- 1 to 8) {
      val v = s(i - 1) match {
        case c if c >= '0' && c <= '9' => c - '0'
        case c if c >= 'A' && c <= 'Z' => c - 'A' + 10
        case '*'                       => 36
        case '@'                       => 37
        case '#'                       => 38
        case x =>
          throw new IllegalStateException(
            s"It should not have been possible for this character to make it through: '$x'"
          )
      }
      val vv = if (i % 2 == 0) v * 2 else v
      sum += (vv / 10) + (vv % 10)
    }
    val digit = (10 - (sum % 10)) % 10
    digit.toString
  }

  def isValidBaseFormatStrict(string: String): Boolean =
    baseFormat.matches(string)

  def isValidBaseFormatLoose(string: String): Boolean =
    baseFormat.matches(string.trim.toUpperCase)

  def isValidIssueFormatStrict(string: String): Boolean =
    issueFormat.matches(string)

  def isValidIssueFormatLoose(string: String): Boolean =
    issueFormat.matches(string.trim.toUpperCase)

  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  def isValidCheckDigitFormatLoose(string: String): Boolean =
    checkDigitFormat.matches(string.trim)

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 9 and each component is the right mix of letters, digits and/or special characters. The apply() method is more
    * permissive, because it will trim leading and/or trailing whitespace and convert to uppercase before validating the
    * CUSIP.
    */
  def isValidCusipFormatStrict(string: String): Boolean =
    cusipFormat.matches(string)

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidCusipFormatLoose(string: String): Boolean =
    cusipFormat.matches(string.trim.toUpperCase)

  def fromParts(
      base: String,
      issue: String,
      checkDigit: String
  ): Either[String, Cusip] = {
    val tempBase = base.trim.toUpperCase
    val tempIssue = issue.trim.toUpperCase
    val tempCheckDigit = checkDigit.trim.toUpperCase

    if (!isValidBaseFormatStrict(tempBase))
      Left(s"Format of base '$base' is not valid")
    else if (!isValidIssueFormatStrict(tempIssue))
      Left(s"Format of issue '$issue' is not valid")
    else if (!isValidCheckDigitFormatStrict(tempCheckDigit))
      Left(s"Format of check digit '$checkDigit' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitUnsafe(base, issue)

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
    val tempBase = base.trim.toUpperCase
    val tempIssue = issue.trim.toUpperCase

    if (!isValidBaseFormatStrict(tempBase))
      Left(s"Format of base '$base' is not valid")
    else if (!isValidIssueFormatStrict(tempIssue))
      Left(s"Format of issue '$issue' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitUnsafe(base, issue)
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
