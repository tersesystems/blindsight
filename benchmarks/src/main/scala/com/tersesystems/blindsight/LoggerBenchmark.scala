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
class LoggerBenchmark {

  val logger: Logger               = LoggerFactory.getLogger
  val infoCondition: Condition     = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val traceConditionLogger: Logger = logger.onCondition(infoCondition)
  val falseConditionLogger: Logger = logger.onCondition(false)
  val helloWorldMessage: Message   = Message("Hello world")

  @Benchmark
  def info(): Unit = {
    // 600 ns with an info statement.
    logger.info(helloWorldMessage.raw)
  }

  @Benchmark
  def trace(): Unit = {
    // 600 ns with an trace statement.
    logger.trace("Hello world")
  }

  @Benchmark
  def falseWhenTrace(): Unit = {
    // 600 ns with an info statement.
    logger.trace.when(false) { log => log("Hello world") }
  }

  @Benchmark
  def conditionalTrace(): Unit = {
    traceConditionLogger.trace("Hello world")
  }

  @Benchmark
  def falseConditionalTrace(): Unit = {
    falseConditionLogger.trace("Hello world")
  }

  @Benchmark
  def ifTrace(): Unit = {
    // 16 ns using conditional logging.
    if (logger.isTraceEnabled()) logger.trace("Hello world")
  }

}
