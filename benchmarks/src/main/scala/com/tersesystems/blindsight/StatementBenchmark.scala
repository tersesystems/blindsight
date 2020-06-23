package com.tersesystems.blindsight

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class StatementBenchmark {

  val arg1 = Argument("one")
  val arg2 = Argument("two")
  val arg3 = Argument("three")

  @Benchmark
  def statementFromInterpolation(blackhole: Blackhole): Unit = {
    blackhole.consume(st"Hello world $arg1, $arg2, $arg3")
  }

  @Benchmark
  def statementFromApply(blackhole: Blackhole): Unit = {
    blackhole.consume(
      Statement(message = "Hello world {}, {}, {}", arguments = Arguments(arg1, arg2, arg3))
    )
  }

  @Benchmark
  def statementFromSeq(blackhole: Blackhole): Unit = {
    blackhole.consume(
      Statement(message = "Hello world {}, {}, {}", arguments = Arguments.fromSeq(Array(arg1, arg2, arg3)))
    )
  }

}
