package com.tersesystems.blindsight

import org.openjdk.jmh.annotations.Benchmark

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import com.tersesystems.blindsight.fluent.FluentLogger

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class SemanticBenchmark {
  val semantic      = LoggerFactory.getLogger.semantic[SampleMessage]
  val sampleMessage = SampleMessage("hello world")

  @Benchmark
  def info(): Unit = {
    semantic.info(sampleMessage)
  }

  @Benchmark
  def infoWhen(): Unit = {
    semantic.info.when(false) { info =>
      info(sampleMessage)
    }
  }

  @Benchmark
  def trace(): Unit = {
    semantic.trace(sampleMessage)
  }

  @Benchmark
  def traceWhen(): Unit = {
    semantic.trace.when(false) { trace =>
      trace(sampleMessage)
    }
  }

}

final case class SampleMessage(messageType: String)

object SampleMessage {
  implicit val toStatement: ToStatement[SampleMessage] = ToStatement { sample =>
    Message(sample.messageType).toStatement
  }
}
