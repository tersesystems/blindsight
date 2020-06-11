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
class SLF4JBenchmark {

  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])
  private val coreLogger  = CoreLogger(slf4jLogger)
  val line: Line          = new Line(42)
  val file: File          = new File("file")
  val enclosing           = new Enclosing("enclosing")

  @Benchmark
  def trace(): Unit = {
    // 4 ns for trace
    slf4jLogger.trace("Hello world")
  }

  @Benchmark
  def info(): Unit = {
    // 74 ns with an info statement.
    slf4jLogger.info("Hello world")
  }

  @Benchmark
  def infoWithMarker(): Unit = {
    // 74 ns with an info statement.
    val markers = coreLogger.sourceInfoBehavior(SLF4JLevel.INFO, line, file, enclosing)
    slf4jLogger.info(markers.marker, "Hello world")
  }

}
