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

  val logger: Logger               = LoggerFactory.getLogger
  val condition: Condition         = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val infoConditionLogger: Logger  = logger.onCondition(condition)
  val neverConditionLogger: Logger = logger.onCondition(Condition.never)

  val arg1 = Argument("one")
  val arg2 = Argument("two")
  val arg3 = Argument("three")
  val args = Arguments(arg1, arg2, arg3)

  @Benchmark
  def trace(): Unit = {
    logger.trace("Hello world")
  }

  @Benchmark
  def traceWithArgs(): Unit = {
    logger.trace("Hello world {}, {}, {}", args)
  }

  @Benchmark
  def traceWithStatement(): Unit = {
    logger.trace(st"Hello world ${arg1}, ${arg2}, ${arg3}")
  }

  @Benchmark
  def traceWhen(): Unit = {
    logger.trace.when(condition) { log => log("Hello world") }
  }

  @Benchmark
  def traceCondition(): Unit = {
    infoConditionLogger.trace("Hello world")
  }

  @Benchmark
  def neverTrace(): Unit = {
    neverConditionLogger.trace("Hello world")
  }

  @Benchmark
  def traceExplicitPredicate(): Unit = {
    if (logger.isTraceEnabled()) logger.trace("Hello world")
  }

  @Benchmark
  def info(): Unit = {
    logger.info("Hello world")
  }

  @Benchmark
  def infoWithArgs(): Unit = {
    logger.info("Hello world {}, {}, {}", args)
  }

  @Benchmark
  def infoWithStatement(): Unit = {
    logger.info(st"Hello world ${arg1}, ${arg2}, ${arg3}")
  }

  @Benchmark
  def infoWhen(): Unit = {
    logger.info.when(condition) { log => log("Hello world") }
  }

  @Benchmark
  def infoCondition(): Unit = {
    infoConditionLogger.info("Hello world")
  }

  @Benchmark
  def neverInfo(): Unit = {
    neverConditionLogger.info("Hello world")
  }

  @Benchmark
  def infoExplicitPredicate(): Unit = {
    if (logger.isInfoEnabled()) logger.info("Hello world")
  }

}
