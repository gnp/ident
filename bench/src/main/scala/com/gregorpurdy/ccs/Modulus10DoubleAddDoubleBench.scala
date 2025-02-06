/*
 * Copyright 2023-2025 Gregor Purdy
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

import org.openjdk.jmh.annotations.*
import org.scalacheck.Gen
import org.scalacheck.rng.Seed

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
class Modulus10DoubleAddDoubleBench {
  val iterations = 100 // 10000
  val size = 1000 // 1000000

  // Generate test data using ScalaCheck
  val cases = Gen
    .listOfN(size, Gen.stringOfN(8, Gen.alphaNumChar))
    .map(_.map(_.toUpperCase))
    .pureApply(Gen.Parameters.default, Seed.random())

  @Benchmark def cusipCalculate(): Unit = for (_ <- 1 to iterations) {
    cases.foreach(input => Modulus10DoubleAddDouble.CusipVariant.calculate(input))
  }

  @Benchmark def cusipCalculateSimple(): Unit = for (_ <- 1 to iterations) {
    cases.foreach(input => Modulus10DoubleAddDouble.CusipVariant.calculateSimple(input))
  }

}
