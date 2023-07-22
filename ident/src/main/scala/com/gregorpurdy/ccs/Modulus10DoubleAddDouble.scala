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

/** This algorithm has two variants for computing _Check Digits_. One ([[CusipVariant]]) is used by [[ident.Cusip]] and
  * [[ident.Figi]] and the other ([[IsinVariant]]) is used by [[ident.Isin]].
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
  * [[CusipVariant.calculate]] matches what [[ident.Cusip]] and [[ident.Figi]] need.
  *
  * [[IsinVariant.calculate]] matches what [[ident.Isin]] needs.
  *
  * These two variants are referred to by the same name in the respective standard documents, but they do not compute
  * the same check digits.
  *
  * The simpler private versions are used internally for tests and as a comparison for performance benchmarks. They are
  * more expensive than the table-driven style but are easier to understand. These implementations map directly to the
  * descriptions in the Scaladoc, where the table-driven ones do not.
  *
  * @todo
  *   Update the next paragraph
  *
  * Benchmarking shows the table-driven implementation to be around 100 times faster than the functional style (on the
  * test system, average run time decreases from around 2,015 ns with the functional style to around 19 ns with the
  * table-driven style). Input-dependent variability in run time decreases also from about +/- 14% for the
  * functional-style to about +/- 3% for the table-driven style.
  */
object Modulus10DoubleAddDouble {

  /** Compute the value of a Character to use in checksum calculations. Only decimal digits and upper- or lower-case
    * letters have defined values. All other Characters result in a value of zero.
    *
    * Decimal digits '0' through '9' map to values 0 through 9, and letter characters 'A' through 'Z' and 'a' through
    * 'z' map to values 10 through 35.
    *
    * @param char
    *   Any character
    * @return
    *   The value of the character for computing the checkum, or zero if it is not an expected character.
    */
  private[ccs] def charValue(char: Char): Int = char match {
    case c if c >= '0' && c <= '9' => c - '0'
    case c if c >= 'A' && c <= 'Z' => c - 'A' + 10
    case c if c >= 'a' && c <= 'z' => c - 'a' + 10
    case _                         => 0
  }

  import scala.language.implicitConversions
  private implicit def int2Byte(i: Int): Byte = i.toByte

  /** This variant is used by [[ident.Cusip]] and [[ident.Figi]].
    *
    * The values of entries in the `Evens` and `Odds` tables can be found by evaluating this Mathematica expression and
    * reading off the values in the "ODD%10" and "EVEN%10" columns:
    *
    * ```mathematica
    * Dataset[Table[<|
    *     "N" -> n, "N/10" -> Quotient[n, 10],
    *     "N%10" -> Mod[n, 10],
    *     "ODD" -> Quotient[n, 10] + Mod[n, 10],
    *     "ODD%10" -> Mod[Quotient[n, 10] + Mod[n, 10], 10],
    *     "N*2" -> n*2, "(N*2)/10" -> Quotient[n * 2, 10],
    *     "(N*2)%10" -> Mod[n * 2, 10],
    *     "EVEN" -> Quotient[n * 2, 10] + Mod[n * 2, 10],
    *     "EVEN%10" -> Mod[Quotient[n * 2, 10] + Mod[n * 2, 10], 10]
    *     |>, {n, 0, 35}]]
    * ```
    */
  object CusipVariant {

    /** The maximum value the accumulator can have and still be able to go another iteration without overflowing. Used
      * to determine when to reduce the accumulator with a modulus operation.
      *
      * The maximum amount that can be added in a single iteration occurs when the underlying character value is 34
      * (letter 'Y') and it is in a doubling position. In that case, the double value is 68, and we add 6 + 8 = 14 to
      * the sum. So, we subtract that value from the maximum u8 value to get the threshold at which we must pre-mod the
      * sum before adding at that step.
      *
      * You can see this easily with the Mathematica code to generate the table:
      *
      * ```mathematica
      * Table[{n, n*2, Quotient[n * 2, 10],
      *     Mod[n * 2, 10], Quotient[n * 2, 10] + Mod[n * 2, 10]}, {n, 0,
      *     35}] // TableForm
      * ```
      */
    private[ccs] val MaxAccumSimple: Byte = Byte.MaxValue - 14

    /** This method requires that `payload` has already been validated to be the right format. Any invalid characters
      * are ignored (do not contribute to the calculated _Check Digit_).
      *
      * The algorithm processes the input characters right-to-left, using [[charValue]] to obtain a value for each
      * character, and counting from the left, doubles values at odd indexes and leaves the rest with their regular
      * values. The sum of these values is reduced mod 10. The final result is (10 - sum) % 10, which is converted to a
      * single decimal digit String.
      *
      * @note
      *   This private implementation is in easier-to-understand imperative style exists to support property-based
      *   testing of the higher performance table-driven implemenation of [[calculate]].
      *
      * @return
      *   the Check Digit (a one-character String)
      */
    private[ccs] def calculateSimple(payload: String): String = {
      var sum: Byte = 0
      for (i <- payload.size - 1 to 0 by -1) {
        val v = charValue(payload(i))
        val vv = if (i % 2 == 1) v * 2 else v
        // Cannot trigger on input < 9 characters long because floor((127 - 14) / 14) = 8.
        if (sum > MaxAccumSimple) sum %= 10
        sum += (vv / 10) + (vv % 10)
      }
      val digit = (10 - (sum % 10)) % 10
      digit.toString
    }

    val MaxAccumTable: Byte = Byte.MaxValue - 9

// format: off
    private val Odds: Array[Byte] = Array(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
        2, 3, 4, 5, 6, 7, 8, 9, 0, 1,
        3, 4, 5, 6, 7, 8
    )
// format: on

// format: off
    private val Evens: Array[Byte] = Array(
        0, 2, 4, 6, 8,
        1, 3, 5, 7, 9,
        2, 4, 6, 8, 0,
        3, 5, 7, 9, 1,
        4, 6, 8, 0, 2,
        5, 7, 9, 1, 3,
        6, 8, 0, 2, 4,
        7
    )
// format: on

    def calculate(payload: String): String = {
      var sum: Byte = 0
      for (i <- 0 until payload.size) {
        val v = charValue(payload(i))
        val vv = if (i % 2 == 1) Evens(v) else Odds(v)
        // Cannot trigger on input < 14 characters long because floor((127 - 9) / 9) = 13.
        if (sum > MaxAccumTable) sum %= 10
        sum += vv
      }
      val digit = (10 - (sum % 10)) % 10
      digit.toString
    }

  }

  /** This variant is used by [[ident.Isin]].
    *
    * The tables are pre-calculated for the net effect each character has on the checksum accumulator at that point and
    * how it effects whether the next character is in a doubling position or not.
    */
  object IsinVariant {

    /** This method requires that `payload` has already been validated to be the right format. Any invalid characters
      * are ignored (do not contribute to the calculated _Check Digit_).
      *
      * @note
      *   This private implementation is in easier-to-understand imperative style exists to support property-based
      *   testing of the higher performance table-driven implemenation of [[calculate]].
      *
      * @return
      *   the Check Digit (a one-character String)
      */
    private[ccs] def calculateSimple(payload: String): String = {
      def timesTwo(x: Int): Seq[Int] = {
        val product = x * 2
        if (product >= 10) Seq(product / 10, product % 10) else Seq(product)
      }

      val sum = payload
        .map(charValue) // Convert characters to their code values (0 - 35)
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

    /** The width in "steps" each char value consumes when processed. All decimal digits have width one, and all letters
      * have width two (because their values are two digits, from 10 to 35 inclusive).
      */
// format: off
    private val Widths: Array[Byte] = Array(
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2,
    )
// format: on

    /** The net value added to the sum for each char value, if the step count (aka index) at the start of processing
      * that character is odd. Odds vs. evens differ because evens go through doubling and potentially splitting into
      * two digits before being summed to make the net value.
      */
// format: off
    private val Odds: Array[Byte] = Array(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        2, 3, 4, 5, 6, 7, 8, 9, 0, 1,
        4, 5, 6, 7, 8, 9, 0, 1, 2, 3,
        6, 7, 8, 9, 0, 1,
    )
// format: on

    /** The net value added to the sum for each char value, if the step count (aka index) at the start of processing
      * that character is even. Odds vs. evens differ because evens go through doubling and potentially splitting into
      * two digits before being summed to make the net value.
      */
// format: off
    private val Evens: Array[Byte] = Array(
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

    /** The maximum value the accumulator can have and still be able to go another iteration without overflowing. Used
      * to determine when to reduce the accumulator with a modulus operation. The max addition of any iteration is 9
      * because we have pre-computed net values that are already mod 10 themselves.
      */
    val MaxAccumTable: Byte = Byte.MaxValue - 9

    /** Compute the _Check Digit_. No attempt is made to ensure the input string is in the ISIN payload format or
      * length.
      */
    def calculate(s: String): String = {
      var sum: Byte = 0
      var idx: Int = 0
      for (c <- s.reverseIterator) {
        val v = charValue(c)
        val w = Widths(v)
        val x = if (idx % 2 == 0) Evens(v) else Odds(v)
        // Cannot trigger on input < 28 bytes long because floor((255 - 9)/9) = 27. Not performing
        // mod every iteration seems to save a few percent on run time.
        if (sum > MaxAccumTable) sum %= 10
        sum += x
        idx += w
      }
      val digit = (10 - (sum % 10)) % 10
      digit.toString
    }

  }

}
