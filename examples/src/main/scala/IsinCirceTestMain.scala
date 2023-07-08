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

import com.gregorpurdy.ident.IdentCirce.*
import com.gregorpurdy.ident.Isin
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.parser.*
import io.circe.syntax.*

object IsinCirceTestMain {

  case class Security(isin: Isin, name: String)
  object Security {
    implicit val securityDecoder: Decoder[Security] = deriveDecoder[Security]
    implicit val securityEncoder: Encoder[Security] = deriveEncoder[Security]
  }

  val inJson = """{
                 |  "isin" : "US0378331005",
                 |  "name" : "Apple Inc."
                 |}""".stripMargin

  def main(args: Array[String]): Unit = {
    println()
    println("ISIN Circe Test Main:")

    val isin = Isin.parse("US0378331005").getOrElse(throw new RuntimeException("Could not parse ISIN"))
    val inSecurity = Security(isin, "Apple Inc.")

    println()
    println("  * Checking that encoding to JSON works as expected...")
    val outJson = inSecurity.asJson.toString
    if (outJson == inJson) {
      println(s"      * Nice! JSON is as expected.")
    } else {
      throw new RuntimeException(s"Oops! JSON is not as expected:\n$outJson\ndoes not match:\n$inJson")
    }

    println()
    println("  * Checking that decoding from JSON works as expected...")
    val outSecurity = decode[Security](inJson).getOrElse(throw new RuntimeException("Could not decode ISIN from JSON"))
    if (outSecurity == inSecurity) {
      println(s"      * Nice! Security is as expected.")
    } else {
      throw new RuntimeException(s"Oops! Security is not as expected:\n$outSecurity\ndoes not match:\n$inSecurity")
    }

    println()
    println("Done.")
    println()
  }

}
