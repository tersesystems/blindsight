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
class LoggingBenchmark {

  val logger: Logger = LoggerFactory.getLogger
  val infoCondition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val traceConditionLogger: Logger = logger.onCondition(infoCondition)
  val falseConditionLogger: Logger = logger.onCondition(false)

  @Benchmark
  def traceBenchmark: Unit = {
    // 600 ns with an info statement.
    logger.trace("Hello world")
  }

  @Benchmark
  def traceWhenBenchmark: Unit = {
    // 600 ns with an info statement.
    logger.trace.when(false) {log => log("Hello world")}
  }

  @Benchmark
  def conditionalTraceBenchmark: Unit = {
    traceConditionLogger.trace("Hello world")
  }

  @Benchmark
  def falseConditionalTraceBenchmark: Unit = {
    falseConditionLogger.trace("Hello world")
  }

  @Benchmark
  def ifTraceBenchmark: Unit = {
    // 16 ns using conditional logging.
    if (logger.isTraceEnabled()) logger.trace("Hello world")
  }

}
