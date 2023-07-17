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

package com.gregorpurdy.ccs

/** This algorithm has multiple variants for computing _Check Digits_. One is used by [[ident.Cusip]] and [[ident.Figi]]
  * and the other is used by [[ident.Isin]].
  *
  * The algorithm is known as "modulus 10 'double-add-double' check digit". The basic idea is to:
  *
  *   1. assign a numeric value to each character
  *   1. split that value into digits
  *   1. iterate over the digits in reverse order
  *   1. double every other digit starting with the right-most one (for [[ident.Isin]]) or starting with
  *      second-from-left (for [[ident.Cusip]] and [[ident.Figi]])
  *   1. split that result into digits
  *   1. sum over all the digits
  *   1. calculate the sum mod 10
  *   1. compute 10 minus that value
  *   1. if the value is 10, return 0 else return the value itself
  *
  * There is one variant in imperitive style [[calculateCheckDigitUnsafe]] used by [[ident.Cusip]] and [[ident.Figi]].
  *
  * There are two other implementations of a variant that matches what [[ident.Isin]] needs, which goes by the same
  * name, but does not compute the same check digit values.
  *
  * One of these implementations is in a functional style, [[calculateCheckDigitUnsafeAltFunctional]] and is used
  * internally for tests and as a comparison for performance benchmarks. It is more expensive than the table-driven
  * style because it does digit expansion on the fly. But, it is easier to understand. The implementation maps directly
  * to the description above.
  *
  * There is also an implementation in a table-driven style, [[calculateCheckDigitUnsafeAltFunctional]] which is the one
  * actually used when parsing and validating ISINs. The tables are pre-calculated for the net effect each character has
  * on the checksum accumulator at that point and how it effects whether the next character is in a doubling position or
  * not.
  *
  * @todo
  *   Update the next paragraph
  *
  * Benchmarking shows the table-driven implementation to be around 100 times faster than the functional style (on the
  * test system, average run time decreases from around 2,015 ns with the functional style to around 19 ns with the
  * table-driven style). Input-dependent variability in run time decreases also from about +/- 14% for the
  * functional-style to about +/- 3% for the table-driven style.
  *
  * The numeric value of a u8 ASCII character. Digit characters '0' through '9' map to values 0 through 9, and letter
  * characters 'A' through 'Z' map to values 10 through 35.
  */
object Modulus10DoubleAddDouble {

  def charValueUnsafe(char: Char): Int = char match {
    case c if c >= '0' && c <= '9' => c - '0'
    case c if c >= 'A' && c <= 'Z' => c - 'A' + 10
    case x =>
      throw new IllegalArgumentException(
        s"It should not have been possible for this character to make it through: '$x'"
      )
  }

  /** This method requires that `payload` has already been validated to be the right format.
    *
    * The only characters allowed in `payload` are:
    *
    *   - Digits '0' to '9'
    *   - Upper-case letters 'A' to 'Z'
    *
    * @note
    *   This variant is used by [[ident.Cusip]] and [[ident.Figi]].
    *
    * @throws IllegalArgumentException
    *   if an illegal character is encountered
    *
    * @return
    *   the Check Digit (a one-character String)
    */
  def calculateCheckDigitUnsafe(payload: String): String = {
    val s = payload
    val l = payload.size
    var sum: Int = 0
    for (i <- 1 to l) {
      val v = charValueUnsafe(s(i - 1))
      val vv = if (i % 2 == 0) v * 2 else v
      sum += (vv / 10) + (vv % 10)
    }
    val digit = (10 - (sum % 10)) % 10
    digit.toString
  }

  /** This method requires that `payload` has already been validated to be the right format.
    *
    * The only characters allowed in `payload` are:
    *
    *   - Digits '0' to '9'
    *   - Upper-case letters 'A' to 'Z'
    *
    * @note
    *   This variant is used by [[ident.Isin]].
    *
    * @throws IllegalArgumentException
    *   if an illegal character is encountered
    *
    * @return
    *   the Check Digit (a one-character String)
    */
  @throws[IllegalArgumentException]("If encountering an unexpected character")
  def calculateCheckDigitUnsafeAltFunctional(payload: String): String = {
    def timesTwo(x: Int): Seq[Int] = {
      val product = x * 2
      if (product >= 10) Seq(product / 10, product % 10) else Seq(product)
    }

    val sum = payload
      .map(charValueUnsafe) // Convert characters to their code values (0 - 36)
      .flatMap { x =>
        if (x >= 10) Seq(x / 10, x % 10) else Seq(x)
      } // Convert two-digit codes to two one-digit codes
      .reverse // Start the alternate multiply-by-two and leave-alone from the right
      .zipWithIndex // Pair each number with an index we can use to drive the alternation
      .flatMap { case (x, i) =>
        if (i % 2 == 0) timesTwo(x) else Seq(x)
      } // Double every other one
      .sum

    val digit = (10 - (sum % 10)) % 10
    digit.toString
  }

  import scala.language.implicitConversions
  implicit def int2Byte(i: Int): Byte = i.toByte

  /** The width in "steps" each char value consumes when processed. All decimal digits have width one, and all letters
    * have width two (because their values are two digits, from 10 to 35 inclusive).
    */
// format: off
  val WIDTHS: Array[Byte] = Array(
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      2, 2, 2, 2, 2, 2,
  )
// format: on

  /** The net value added to the sum for each char value, if the step count (aka index) at the start of processing that
    * character is odd. Odds vs. evens differ because evens go through doubling and potentially splitting into two
    * digits before being summed to make the net value.
    */
// format: off
  val ODDS: Array[Byte] = Array(
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
      2, 3, 4, 5, 6, 7, 8, 9, 0, 1,
      4, 5, 6, 7, 8, 9, 0, 1, 2, 3,
      6, 7, 8, 9, 0, 1,
  )
// format: on

  /** The net value added to the sum for each char value, if the step count (aka index) at the start of processing that
    * character is even. Odds vs. evens differ because evens go through doubling and potentially splitting into two
    * digits before being summed to make the net value.
    */
// format: off
  val EVENS: Array[Byte] = Array(
    0, 2, 4, 6, 8,
    1, 3, 5, 7, 9,
    1, 3, 5, 7, 9,
    2, 4, 6, 8, 0,
    2, 4, 6, 8, 0,
    3, 5, 7, 9, 1,
    3, 5, 7, 9, 1,
    4,
  )
// format: on

  /** The maximum value the accumulator can have and still be able to go another iteration without overflowing. Used to
    * determine when to reduce the accumulator with a modulus operation. The max addition of any iteration is 9 because
    * we have pre-computed net values that are already mod 10 themselves.
    */
  val MAX_ACCUM: Byte = Byte.MaxValue - 9

  /** Compute the _checksum_ for a u8 array. No attempt is made to ensure the input string is in the ISIN payload format
    * or length.
    *
    * # Panics
    *
    * If an illegal character (not an ASCII digit and not an ASCII uppercase letter) is encountered, the char_value()
    * function this calls will panic.
    */
  def calculateCheckDigitUnsafeAltTable(s: String): String = {
    var sum: Byte = 0
    var idx: Int = 0
    for (c <- s.reverseIterator) {
      val v = charValueUnsafe(c)
      val w = WIDTHS(v)
      val x = if ((idx % 2) == 0) {
        EVENS(v)
      } else {
        ODDS(v)
      }
      // Cannot trigger on input < 28 bytes long because floor((255 - 9)/9) = 27. Not performing
      // mod every iteration seems to save a few percent on run time.
      if (sum > MAX_ACCUM) {
        sum %= 10
      }
      sum += x
      idx += w
    }
    sum %= 10

    val diff = 10 - sum
    val temp = if (diff == 10) {
      0
    } else {
      diff
    }
    temp.toString
  }

}
