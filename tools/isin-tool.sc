#!/usr/bin/env -S scala-cli shebang -S 3
//> using scala 3.6.3
//> using dep "com.gregorpurdy::ident:0.4.0-SNAPSHOT"

//
// Copyright 2023-2025 Gregor Purdy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

import com.gregorpurdy.ident.Isin
import com.gregorpurdy.ident.IsinError

import scala.io.Source

import System.exit

def die(message: String, status: Int): Nothing = {
  System.err.println(message)
  exit(status)
  throw new IllegalStateException("System.exit should not have returned")
}

val fix: Boolean = if (args.length == 0) {
  false
} else if (args.length == 1 && args(0) == "--fix") {
  true
} else {
  die("usage: isin-tool.sc [--fix]]", 1)
}

var good: Long = 0L
var bad: Long = 0L
var fixed: Long = 0L

val source = Source.fromInputStream(System.in)
for ((line, pos) <- source.getLines().zipWithIndex) {
  Isin.fromString(line) match {
    case Right(isin) => {
      good += 1L
      if (fix) println(isin)
    }
    case Left(err : IsinError.IncorrectCheckDigitValue) => {
      bad += 1L
      if (fix) {
        val isin = Isin.fromPayload(line.trim.substring(0, 11)).toOption.get
        println(isin.toString)
        fixed += 1L
        System.err.println(s"Input [${pos + 1}]: $line; Error: $err [FIXED]")
      } else {
        System.err.println(s"Input [${pos + 1}]: $line; Error: $err")
      }
    }
    case Left(err) => {
      bad += 1L
      System.err.println(s"Input [${pos + 1}]: $line; Error: $err")
    }
  }
}

if (fix) {
  System.err.println(
    s"Read ${good + bad} values; $good were valid ISINs and $bad were not. Fixed $fixed; Omitted ${bad - fixed}."
  )
  if (bad > fixed) exit(1) else exit(0)
} else {
  System.err.println(
    s"Read ${good + bad} values; $good were valid ISINs and $bad were not."
  )
  if (bad != 0L) exit(1) else exit(0)
}
