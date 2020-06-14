package com.tersesystems.blindsight

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import com.tersesystems.blindsight.flow.{FlowBehavior, SimpleFlowBehavior}
import com.tersesystems.blindsight.fluent.FluentLogger
import org.openjdk.jmh.infra.Blackhole

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class FlowBenchmark {
  val flow = LoggerFactory.getLogger.flow

  implicit val flowBehavior: FlowBehavior[String] = new SimpleFlowBehavior[String]

  @Benchmark
  def info(blackhole: Blackhole): Unit = {
    blackhole.consume(flow.info("hello".concat(" world")))
  }

  @Benchmark
  def infoWhen(blackhole: Blackhole): Unit = {
    val result = flow.info.when(false) {
      "hello".concat(" world")
    }
    blackhole.consume(result)
  }

  @Benchmark
  def trace(blackhole: Blackhole): Unit = {
    blackhole.consume(flow.trace("hello".concat(" world")))
  }

  @Benchmark
  def traceWhen(blackhole: Blackhole): Unit = {
    val result = flow.trace.when(false) {
      "hello ".concat(" world")
    }
    blackhole.consume(result)
  }

}
