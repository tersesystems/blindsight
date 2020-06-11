package com.tersesystems.blindsight

import java.util.concurrent.TimeUnit

import com.tersesystems.blindsight.slf4j.SLF4JLogger
import org.openjdk.jmh.annotations.{
  Benchmark,
  BenchmarkMode,
  Fork,
  Measurement,
  Mode,
  OutputTimeUnit,
  Scope,
  State,
  Warmup
}
import sourcecode.{Enclosing, File, Line}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class StrictLoggerBenchmark {
  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])
  private val coreLogger  = CoreLogger(slf4jLogger)
  val line: Line          = new Line(42)
  val file: File          = new File("file")
  val enclosing           = new Enclosing("enclosing")

  val strict = new SLF4JLogger.Strict(coreLogger)

  @Benchmark
  def info(): Unit = {
    // 74 ns with an info statement.
    strict.info("Hello world")
  }
}
