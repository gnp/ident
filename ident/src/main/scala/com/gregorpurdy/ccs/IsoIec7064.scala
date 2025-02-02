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

object IsoIec7064 {

  /** This algorithm uses as MAX value computed from Long.MaxValue by subtracting the numeric value of 'Z' (35) and
    * dividing by 100", so we can use MAX as a threshold to compute the modulus before we are done, but never as long as
    * there is still room to multiply by 100 and add 36 without exceeding Long.MaxValue.
    *
    * The value is precomputed to be the same as `(Long.MaxValue - Character.getNumericValue('Z')) / 100`.
    */
  private[ccs] final val Max = 92233720368547757L // (Long.MaxValue - Character.getNumericValue('Z')) / 100

  private final val Modulus = 97

  /** Computes the ISO/IEC 7064, MOD 97-10 check digit for a given string.
    *
    * @param string
    *   you can call this with a complete identifier with check digits already appended, and check that the result is 1
    *   as part of verifying that the identifier is valid. Be sure the input string is purely alphanumeric.
    */
  def mod97_10(string: String): Int = {
    val temp = string
      .map(Character.getNumericValue(_).toLong)
      .fold(0L) { (total, charValue) =>
        val factor = if (charValue > 9) 100 else 10
        val nextValue = total * factor + charValue
        if (nextValue > Max) nextValue % Modulus else nextValue
      }

    (temp % Modulus).toInt
  }

}
