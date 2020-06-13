# Results

On the following hardware:

```text
Processor	Intel(R) Core(TM) i9-9900K CPU @ 3.60GHz, 3600 Mhz, 8 Core(s), 16 Logical Processor(s)
```

With the following benchmark:

```scala
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
  val condition: Condition     = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val infoConditionLogger: Logger = logger.onCondition(condition)
  val falseConditionLogger: Logger = logger.onCondition(false)

  @Benchmark
  def trace(): Unit = {
    logger.trace("Hello world")
  }

  @Benchmark
  def traceWhen(): Unit = {
    logger.trace.when(false) { log => log("Hello world") }
  }

  @Benchmark
  def traceCondition(): Unit = {
    infoConditionLogger.trace("Hello world")
  }

  @Benchmark
  def traceFalse(): Unit = {
    falseConditionLogger.trace("Hello world")
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
  def infoWhen(): Unit = {
    // 600 ns with an info statement.
    logger.info.when(false) { log => log("Hello world") }
  }

  @Benchmark
  def infoCondition(): Unit = {
    infoConditionLogger.info("Hello world")
  }

  @Benchmark
  def infoFalse(): Unit = {
    falseConditionLogger.info("Hello world")
  }

  @Benchmark
  def infoExplicitPredicate(): Unit = {
    if (logger.isInfoEnabled()) logger.info("Hello world")
  }

}
```

Using a no-op appender and Logback:

```text
[info] LoggingBenchmark.info                    avgt    5  100.717 ± 2.510  ns/op
[info] LoggingBenchmark.infoCondition           avgt    5  138.205 ± 5.585  ns/op
[info] LoggingBenchmark.infoExplicitPredicate   avgt    5  114.492 ± 4.629  ns/op
[info] LoggingBenchmark.infoFalse               avgt    5   12.155 ± 0.247  ns/op
[info] LoggingBenchmark.infoWhen                avgt    5    1.947 ± 0.052  ns/op
[info] LoggingBenchmark.trace                   avgt    5    4.335 ± 0.179  ns/op
[info] LoggingBenchmark.traceCondition          avgt    5   18.360 ± 0.292  ns/op
[info] LoggingBenchmark.traceExplicitPredicate  avgt    5    4.186 ± 0.024  ns/op
[info] LoggingBenchmark.traceFalse              avgt    5   12.181 ± 0.366  ns/op
[info] LoggingBenchmark.traceWhen               avgt    5    1.947 ± 0.008  ns/op
```