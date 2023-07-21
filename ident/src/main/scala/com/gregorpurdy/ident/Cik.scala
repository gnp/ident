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

/** An SEC / EDGAR Central Index Key (CIK) number is a 10-digit numerical identifier associated with every entity that
  * files with the SEC.
  */
final case class Cik private (value: Long) {

  override def toString: String = value.toString

  def toStringPadded: String = f"${value}%010d"

  def toStringTagged: String = s"cik:$value"

}

object Cik extends CikVersionSpecific {

  private val cikFormat = "([1-9][0-9]{0,9})".r

  val MaxCIK: Long = 9999999999L

  def isValidFormatStrict(string: String): Boolean =
    cikFormat.matches(string)

  def isValidFormat(string: String): Boolean =
    cikFormat.matches(string.trim.replaceFirst("^0*", ""))

  def fromLong(value: Long): Either[String, Cik] = {
    if (value == 0)
      Left("CIK cannot be zero")
    else if (value < 0)
      Left(s"CIK cannot be negative: $value")
    else if (value > MaxCIK)
      Left(s"CIK cannot be more than 10 digits: $value)")
    else
      Right(new Cik(value))
  }

  def fromString(value: String): Either[String, Cik] = {
    val temp = normalize(value)
    if (temp.exists(!_.isDigit))
      Left(s"Input string contains non-digit characters: '$value'")
    else if (temp.length > 10)
      Left(s"Input string has more than 10 digits: '$value'")
    else {
      val v = temp.replaceFirst("^0*", "").toLong
      fromLong(v)
    }
  }

}
