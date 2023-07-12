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
  *   https://en.wikipedia.org/wiki/Financial_Instrument_Global_Identifier
  *
  * NOTE: Wikipedia page mentions the provider can be any two letters except "BS, BM, GG, GB, GH, KY, VG" but I could
  * not find anything about that on the openfigi web site.
  *
  * @see
  *   https://www.omg.org/spec/FIGI/1.0/PDF
  *
  * NOTE: Section 2.1, page 7 says of the first two digits: "Qualification: positions 1 and 2 cannot be the following
  * sequences: BS, BM, GG, GB, VG.". Later in Section 6.1.2, page 16 says "BS, BM, GG, GB, GH, KY, VG" are forbidden, as
  * does the Wikipedia page. This same list comes up again in Section 8.2.4, page 34-35.
  *
  * @see
  *   https://www.openfigi.com/about/figi
  * @see
  *   https://www.openfigi.com/assets/content/figi-check-digit-2173341b2d.pdf
  * @see
  *   https://www.openfigi.com/assets/local/figi-allocation-rules.pdf
  */
final case class Figi private (value: String) {

  def provider: String = value.substring(0, 2)

  def scope: String = value.substring(2, 3)

  def id: String = value.substring(3, 11)

  def checkDigit: String = value.substring(11, 12)

  override def toString: String = value

  def toStringTagged: String = s"figi:$value"

}

object Figi {

  implicit val ord: Ordering[Figi] = Ordering.by(_.value)

  val providerFormat: Regex = "[B-DF-HJ-NP-TV-Z0-9]{2}".r
  val providerExclusions: Set[String] = Set("BS", "BM", "GG", "GB", "GH", "KY", "VG")
  val scopeFormat: Regex = "G".r
  val idFormat: Regex = "[B-DF-HJ-NP-TV-Z0-9]{8}".r
  val checkDigitFormat: Regex = "[0-9]".r
  val figiFormat: Regex = "([B-DF-HJ-NP-TV-Z0-9]{2})(G)([B-DF-HJ-NP-TV-Z0-9]{8})([0-9])".r

  def calculateCheckDigit(
      provider: String,
      scope: String,
      id: String
  ): String = {
    val tempProvider = provider.trim.toUpperCase
    val tempScope = scope.trim.toUpperCase
    val tempId = id.trim.toUpperCase

    if (!isValidProviderFormatStrict(tempProvider))
      throw new IllegalArgumentException(
        s"Format of provider '$provider' is not valid"
      )

    if (!isValidScopeFormatStrict(tempScope))
      throw new IllegalArgumentException(
        s"Format of scope '$scope' is not valid"
      )

    if (!isValidIdFormatStrict(tempId))
      throw new IllegalArgumentException(
        s"Format of id '$id' is not valid"
      )

    calculateCheckDigitUnsafe(tempProvider, tempScope, tempId)
  }

  /** This method is used internally when the base and issue have already been validated to be the right format.
    */
  private def calculateCheckDigitUnsafe(
      provider: String,
      scope: String,
      id: String
  ): String = {
    val s = s"$provider$scope$id"
    var sum: Int = 0
    for (i <- 1 to 11) {
      val v = s(i - 1) match {
        case c if c >= '0' && c <= '9' => c - '0'
        case c if c >= 'A' && c <= 'Z' => c - 'A' + 10
        case x =>
          throw new IllegalStateException(
            s"It should not have been possible for this character to make it through: '$x'"
          )
      }
      val vv = if (i % 2 == 0) v * 2 else v
      sum += (vv / 10) + (vv % 10)
    }
    val digit = (10 - (sum % 10)) % 10
//    val digit = 10 - (sum % 10)
    digit.toString
  }

  def isValidProviderFormatStrict(string: String): Boolean =
    providerFormat.matches(string) && !providerExclusions.contains(string)

  def isValidProviderFormatLoose(string: String): Boolean =
    isValidProviderFormatStrict(string.trim.toUpperCase)

  def isValidScopeFormatStrict(string: String): Boolean =
    scopeFormat.matches(string)

  def isValidScopeFormatLoose(string: String): Boolean =
    isValidScopeFormatStrict(string.trim.toUpperCase)

  def isValidIdFormatStrict(string: String): Boolean =
    idFormat.matches(string)

  def isValidIdFormatLoose(string: String): Boolean =
    isValidIdFormatStrict(string.trim.toUpperCase)

  def isValidCheckDigitFormatStrict(string: String): Boolean =
    checkDigitFormat.matches(string)

  def isValidCheckDigitFormatLoose(string: String): Boolean =
    isValidCheckDigitFormatStrict(string.trim)

  /** This will only return true if the input String has no whitespace, all letters are already uppercase, the length is
    * 12 and each component is the right mix of letters, digits and/or special characters. The apply() method is more
    * permissive, because it will trim leading and/or trailing whitespace and convert to uppercase before validating the
    * CUSIP.
    */
  def isValidFigiFormatStrict(string: String): Boolean =
    figiFormat.matches(string) && !providerExclusions.contains(string.substring(0, 2))

  /** This returns true if the input String would be allowed as an argument to the apply() method.
    */
  def isValidFigiFormatLoose(string: String): Boolean =
    isValidFigiFormatStrict(string.trim.toUpperCase)

  def fromParts(
      provider: String,
      scope: String,
      id: String,
      checkDigit: String
  ): Either[String, Figi] = {
    val tempProvider = provider.trim.toUpperCase
    val tempScope = scope.trim.toUpperCase
    val tempId = id.trim.toUpperCase
    val tempCheckDigit = checkDigit.trim.toUpperCase

    if (!isValidProviderFormatStrict(tempProvider))
      Left(s"Format of provider '$provider' is not valid")
    else if (!isValidScopeFormatStrict(tempScope))
      Left(s"Format of scope '$scope' is not valid")
    else if (!isValidIdFormatStrict(tempId))
      Left(s"Format of id '$id' is not valid")
    else if (!isValidCheckDigitFormatStrict(tempCheckDigit))
      Left(s"Format of check digit '$checkDigit' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitUnsafe(provider, scope, id)
      if (tempCheckDigit != correctCheckDigit)
        Left(
          s"Check digit '$checkDigit' is not correct for provider '$provider', scope '$scope' and id '$id'. It should be '$correctCheckDigit'"
        )
      else
        Right(new Figi(s"$provider$scope$id$tempCheckDigit"))
    }
  }

  /** Create a FIGI from a provider, scope and id, computing the correct check digit automatically.
    */
  def fromPartsCalcCheckDigit(provider: String, scope: String, id: String): Either[String, Figi] = {
    val tempProvider = provider.trim.toUpperCase
    val tempScope = scope.trim.toUpperCase
    val tempId = id.trim.toUpperCase

    if (!isValidProviderFormatStrict(tempProvider))
      Left(s"Format of provider '$provider' is not valid")
    else if (!isValidScopeFormatStrict(tempScope))
      Left(s"Format of scope '$scope' is not valid")
    else if (!isValidIdFormatStrict(tempId))
      Left(s"Format of id '$id' is not valid")
    else {
      val correctCheckDigit = calculateCheckDigitUnsafe(provider, scope, id)
      Right(new Figi(s"$provider$scope$id$correctCheckDigit"))
    }
  }

  def fromString(value: String): Either[String, Figi] =
    normalize(value) match {
      case figiFormat(provider, scope, id, checkDigit) =>
        fromParts(provider, scope, id, checkDigit)
      case _ =>
        Left(s"Input string is not in valid FIGI format: '$value'")
    }

}
