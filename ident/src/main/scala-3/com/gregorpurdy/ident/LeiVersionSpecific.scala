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

import scala.util.CommandLineParser

abstract class LeiVersionSpecific {

  given Ordering[Lei] = Ordering.by(_.value)

  object LeiCommandLineParserFromString extends CommandLineParser.FromString[Lei] {
    def fromString(s: String): Lei = Lei.fromString(s) match {
      case Left(s)      => throw new IllegalArgumentException(s)
      case Right(ident) => ident
    }
    override def fromStringOption(s: String): Option[Lei] = Lei.fromString(s).toOption
  }

  given CommandLineParser.FromString[Lei] = LeiCommandLineParserFromString

}
