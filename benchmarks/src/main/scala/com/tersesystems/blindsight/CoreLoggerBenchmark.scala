package com.tersesystems.blindsight

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import org.slf4j.event.{Level => SLF4JLevel}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class CoreLoggerBenchmark {

  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])
  private val coreLogger = CoreLogger(slf4jLogger)
  val logger: Logger = LoggerFactory.getLogger(slf4jLogger)

  @Benchmark
  def slf4jBenchmark: Unit = {
    // 4 ns for trace
    slf4jLogger.trace("Hello world")
  }

  @Benchmark
  def coreBenchmark: Unit = {
    // 4 ns for trace
    coreLogger.parameterList(SLF4JLevel.TRACE).message("Hello world")
  }

  @Benchmark
  def disabledLoggerBenchmark: Unit = {
    // 600 ns with an info statement.
    logger.trace("Hello world")
  }

  @Benchmark
  def enabledLoggerBenchmark: Unit = {
    // 600 ns with an info statement.
    logger.info("Hello world")
  }

}
