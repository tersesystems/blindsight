package com.tersesystems.blindsight

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import com.tersesystems.blindsight.flow.FlowBehavior

import org.slf4j.event.{Level => SLF4JLevel}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class FlowBenchmark {
  val flow                 = LoggerFactory.getLogger.flow
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val neverLogger          = flow.onCondition(Condition.never)

  implicit def flowBehavior[B]: FlowBehavior[B] = FlowBehavior.noop

  @Benchmark
  def info(): Unit = {
    val result = flow.info {
      "Hello world"
    }
  }

  @Benchmark
  def infoWhen(): Unit = {
    val result = flow.info.when(condition) {
      "Hello world"
    }
  }

  @Benchmark
  def neverInfo(): Unit = {
    val result = neverLogger.info {
      "Hello world"
    }
  }

  @Benchmark
  def trace(): Unit = {
    val result = flow.trace {
      "Hello world"
    }
  }

  @Benchmark
  def traceWhen(): Unit = {
    val result = flow.trace.when(condition) {
      "Hello world"
    }
  }

  @Benchmark
  def neverTrace(): Unit = {
    val result = neverLogger.trace {
      "Hello world"
    }
  }

}
