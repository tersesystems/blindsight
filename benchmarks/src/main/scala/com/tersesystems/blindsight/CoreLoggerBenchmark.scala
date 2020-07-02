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
  val line: Line          = new Line(42)
  val file: File          = new File("file")
  val enclosing           = new Enclosing("enclosing")

  val arg1 = Argument("one")
  val arg2 = Argument("two")
  val arg3 = Argument("three")
  val args = Arguments(arg1, arg2, arg3)

  @Benchmark
  def trace(): Unit = {
    // 2.715 ns for trace
    coreLogger.parameterList(SLF4JLevel.TRACE).message("Hello world")
  }

  @Benchmark
  def traceWithStatement(): Unit = {
    coreLogger
      .parameterList(SLF4JLevel.TRACE)
      .executeStatement((st"Hello world ${arg1}, ${arg2}, ${arg3}"))
  }

  @Benchmark
  def info(): Unit = {
    coreLogger.parameterList(SLF4JLevel.INFO).message("Hello world")
  }

  @Benchmark
  def infoWithStatement(): Unit = {
    coreLogger
      .parameterList(SLF4JLevel.INFO)
      .executeStatement(st"Hello world ${arg1}, ${arg2}, ${arg3}")
  }

}
