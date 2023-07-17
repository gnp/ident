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

/** The algorithm with variants used by [[ident.Cusip]] and [[ident.Isin]] for computing their _Check Digits_ */
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

}
