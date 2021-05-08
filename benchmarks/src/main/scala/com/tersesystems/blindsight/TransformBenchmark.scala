package com.tersesystems.blindsight

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class TransformBenchmark {

  private val b50    = EventBuffer(50)
  private val b50000 = EventBuffer(50000)

  private val transformF: (Entry => Entry) = { e =>
    e.copy(message = e.message + " IN BED")
  }

  val logger = LoggerFactory.getLogger

  val transformLogger: Logger = logger.withEntryTransform(transformF)

  val buffer50Logger: Logger = logger.withEventBuffer(b50)

  val buffer50000Logger: Logger = logger.withEventBuffer(b50000)

  @Benchmark
  def transform(): Unit = {
    transformLogger.info("Hello world")
  }

  @Benchmark
  def buffer50(): Unit = {
    buffer50Logger.info("Hello world")
  }

  @Benchmark
  def buffer50000(): Unit = {
    buffer50000Logger.info("Hello world")
  }

}
