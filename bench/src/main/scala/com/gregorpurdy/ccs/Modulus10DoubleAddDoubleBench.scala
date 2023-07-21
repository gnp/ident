package com.gregorpurdy.ccs

import org.openjdk.jmh.annotations.*
import zio.test.*
import zio.{Runtime, Unsafe}
// import zio.ZLayer

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
class Modulus10DoubleAddDoubleBench {
  val iterations = 10000
  val size = 1000000
  val effect = Gen.alphaNumericStringBounded(8, 8).sample.take(size).map(_.value.toUpperCase).runCollect
  val cases = Unsafe
    .unsafe { implicit unsafe =>
      zio.Runtime.default.unsafe
        .run(
          effect // .provide(ZLayer.succeed(TestRandom))
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
