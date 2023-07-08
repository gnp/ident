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

import com.gregorpurdy.ident.ISIN
import com.gregorpurdy.ident.IsinZioSchema._
import zio._
import zio.schema.DeriveSchema
import zio.schema.Schema
import zio.schema.codec.JsonCodec._

object IsinZioSchemaTestMain extends ZIOAppDefault {

  case class Security(isin: ISIN, name: String)
  object Security {
    implicit val schema: Schema[Security] = DeriveSchema.gen[Security]
  }

  def run: ZIO[Any, Any, Unit] = for {
    _ <- Console.printLine("")
    _ <- Console.printLine("ISIN ZIO Schema Test Main:")
    isin <- ZIO.fromEither(ISIN.parse("US0378331005"))
    inSecurity = Security(isin, "Apple Inc.")
    outJson = jsonCodec(Security.schema).encodeJson(inSecurity, None)
    _ <- Console.printLine(s"Encoded JSON: $outJson")
    outSecurity = jsonCodec(Security.schema)
      .decodeJson(outJson)
      .getOrElse(throw new RuntimeException("Could not parse"))
    _ <- Console.printLine(s"Decoded Security: $outSecurity")
  } yield ()

}
