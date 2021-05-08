package com.tersesystems.blindsight

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class StatementBenchmark {

  //    @Benchmark
  //    def statementFromInterpolation(blackhole: Blackhole): Unit = {
  //      val arg1 = "one"
  //      val arg2 = "two"
  //      val arg3 = "three"
  //      blackhole.consume(st"Hello world $arg1, $arg2, $arg3")
  //    }

  @Benchmark
  def statementFromApply(blackhole: Blackhole): Unit = {
    val arg1 = "one"
    val arg2 = "two"
    val arg3 = "three"
    blackhole.consume(
      Statement(message = "Hello world {}, {}, {}", arguments = Arguments(arg1, arg2, arg3))
    )
  }

  @Benchmark
  def statementFromArray(blackhole: Blackhole): Unit = {
    val arg1 = "one"
    val arg2 = "two"
    val arg3 = "three"
    blackhole.consume(
      Statement(
        message = "Hello world {}, {}, {}",
        arguments = Arguments.fromArray(Array(Argument(arg1), Argument(arg2), Argument(arg3)))
      )
    )
  }

}
