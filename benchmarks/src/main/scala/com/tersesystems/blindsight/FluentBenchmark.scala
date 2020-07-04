package com.tersesystems.blindsight

import com.tersesystems.blindsight.fluent.FluentLogger
import org.openjdk.jmh.annotations.Benchmark

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import com.tersesystems.blindsight.fluent.FluentLogger

import org.slf4j.event.{Level => SLF4JLevel}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class FluentBenchmark {
  val fluent: FluentLogger = LoggerFactory.getLogger.fluent
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val neverConditionLogger = fluent.withCondition(Condition.never)

  val arg1 = Argument("one")
  val arg2 = Argument("two")
  val arg3 = Argument("three")
  val args = Arguments(arg1, arg2, arg3)

  @Benchmark
  def info(): Unit = {
    fluent.info.message("Hello world").log()
  }

  @Benchmark
  def infoWithArgs(): Unit = {
    fluent.info
      .statement(st"Hello world {}, {}, {}")
      .argument(arg1)
      .argument(arg2)
      .argument(arg3)
      .log()
  }

  @Benchmark
  def infoWithStatement(): Unit = {
    fluent.info.statement(st"Hello world ${arg1}, ${arg2}, ${arg3}").log()
  }

  @Benchmark
  def infoWhen(): Unit = {
    fluent.info.when(condition) { info =>
      info.message("Hello world").log()
    }
  }

  @Benchmark
  def neverWhen(): Unit = {
    neverConditionLogger.info.message("Hello world").log()
  }

  @Benchmark
  def trace(): Unit = {
    fluent.trace.message("Hello world").log()
  }

  @Benchmark
  def neverTrace(): Unit = {
    neverConditionLogger.trace.message("Hello world").log()
  }

  @Benchmark
  def traceWhen(): Unit = {
    fluent.trace.when(condition) { trace =>
      trace.message("Hello world").log()
    }
  }

}
