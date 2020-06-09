package com.tersesystems.blindsight

import com.tersesystems.blindsight.fluent.FluentLogger
import org.openjdk.jmh.annotations.Benchmark

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import com.tersesystems.blindsight.fluent.FluentLogger

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class FluentBenchmark {
  val fluent: FluentLogger = LoggerFactory.getLogger.fluent

  @Benchmark
  def fluentInfoBenchmark: Unit = {
    fluent.info.message("Hello world").log()
  }

  @Benchmark
  def fluentInfoWhenBenchmark: Unit = {
    fluent.info.when(false) { info =>
      info.message("Hello world").log()
    }
  }

  @Benchmark
  def fluentTraceBenchmark: Unit = {
    fluent.trace.message("Hello world").log()
  }

  @Benchmark
  def fluentTraceWhenBenchmark: Unit = {
    fluent.trace.when(false) { trace =>
      trace.message("Hello world").log()
    }
  }

}
