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

import com.gregorpurdy.ident.Isin

object IsinTestMain {
  val isinString = "US0378331005"
  def main(args: Array[String]): Unit = {
    Isin.parse(isinString) match {
      case Right(isin) =>
        println(s"Parsed ISIN: $isin"); // "US0378331005"
        println(s"  Country code: ${isin.countryCode}"); // "US"
        println(s"  Security identifier: ${isin.securityIdentifier}"); // "037833100"
        println(s"  Check digit: ${isin.checkDigit}"); // '5'
      case Left(err) =>
        throw new RuntimeException(s"Unable to parse ISIN $isinString: $err")
    }
  }
}
