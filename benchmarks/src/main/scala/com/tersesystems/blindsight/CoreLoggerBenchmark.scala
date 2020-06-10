package com.tersesystems.blindsight

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.infra.Blackhole
import org.slf4j.event.{Level => SLF4JLevel}
import sourcecode.{Enclosing, File, Line}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class CoreLoggerBenchmark {

  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])
  private val coreLogger  = CoreLogger(slf4jLogger)
  val logger: Logger      = LoggerFactory.getLogger(slf4jLogger)
  val line: Line          = new Line(42)
  val file: File          = new File("file")
  val enclosing           = new Enclosing("enclosing")

  @Benchmark
  def coreBenchmark: Unit = {
    // 4 ns for trace
    coreLogger.parameterList(SLF4JLevel.TRACE).message("Hello world")
  }

  @Benchmark
  def sourceInfoBehavior(blackhole: Blackhole): Unit = {
    blackhole.consume(coreLogger.sourceInfoBehavior(SLF4JLevel.INFO, line, file, enclosing))
  }

  //  @Benchmark
  //  def slf4jBenchmark: Unit = {
  //    // 4 ns for trace
  //    slf4jLogger.trace("Hello world")
  //  }

  @Benchmark
  def enabledSLF4JLoggerBenchmark: Unit = {
    // 74 ns with an info statement.
    val markers = coreLogger.sourceInfoBehavior(SLF4JLevel.INFO, line, file, enclosing)
    slf4jLogger.info(markers.marker, "Hello world")
  }

  //  @Benchmark
  //  def disabledLoggerBenchmark: Unit = {
  //    // 600 ns with an info statement.
  //    logger.trace("Hello world")
  //  }

  @Benchmark
  def whyIsThisSlow: Unit = {
    // 600 ns with an info statement.
    logger.info("Hello world")
  }

}
