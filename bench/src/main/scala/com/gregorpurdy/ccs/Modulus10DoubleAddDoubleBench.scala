package com.gregorpurdy.ccs

import org.openjdk.jmh.annotations.*
import zio.Runtime
import zio.Unsafe
import zio.test.*

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
class Modulus10DoubleAddDoubleBench {
  val iterations = 10000
  val size = 1000000
  val effect = Gen.alphaNumericStringBounded(8, 8).runCollectN(size).map(_.map(_.toUpperCase))

  val cases = Unsafe
    .unsafe { implicit unsafe =>
      zio.Runtime.default.unsafe
        .run(
          effect.provideLayer(TestRandom.deterministic)
        )
        .getOrThrowFiberFailure()
    }

  @Benchmark def cusipCalculate: Unit = for (_ <- 1 to iterations) {
    cases.foreach(input => Modulus10DoubleAddDouble.CusipVariant.calculate(input))
  }

  @Benchmark def cusipCalculateSimple: Unit = for (_ <- 1 to iterations) {
    cases.foreach(input => Modulus10DoubleAddDouble.CusipVariant.calculateSimple(input))
  }

}
