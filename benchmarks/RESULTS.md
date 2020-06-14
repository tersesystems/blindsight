# Results

This page gives a baseline for how much overhead Blindsight adds on top of using raw SLF4J.  This is purely CPU bound, so your results will vary depending on how powerful your CPU is.  The raw SLF4J logger performance numbers are

## Hardware

Tests are run on a desktop CPU running Windows 10, using an Ubuntu Virtualbox:

```text
Processor	Intel(R) Core(TM) i9-9900K CPU @ 3.60GHz, 3600 Mhz, 8 Core(s), 16 Logical Processor(s)
```

## Raw SLF4J Logger

Running SLF4J as a baseline with Logback 1.2.3 and a no-op appender:

```scala
class SLF4JLoggerBenchmark {
  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])

  @Benchmark
  def trace(): Unit = {
    slf4jLogger.trace("Hello world")
  }

  @Benchmark
  def info(): Unit = {
    slf4jLogger.info("Hello world")
  }
}
```

yields:

```
[info] Benchmark                   Mode  Cnt   Score   Error  Units
[info] SLF4JLoggerBenchmark.info   avgt    5  42.381 ± 0.411  ns/op
[info] SLF4JLoggerBenchmark.trace  avgt    5   1.313 ± 0.044  ns/op
[success] Total time: 23 s, completed Jun 13, 2020, 2:20:01 PM
```

## Core Logger

The core logger should give as close as possible performance to the SLF4J logger.

```scala
class CoreLoggerBenchmark {
  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])
  private val coreLogger  = CoreLogger(slf4jLogger)
  val line: Line          = new Line(42)
  val file: File          = new File("file")
  val enclosing           = new Enclosing("enclosing")

  @Benchmark
  def trace(): Unit = {
    coreLogger.parameterList(SLF4JLevel.TRACE).message("Hello world")
  }

  @Benchmark
  def info(): Unit = {
    coreLogger.parameterList(SLF4JLevel.INFO).message("Hello world")
  }

  @Benchmark
  def sourceInfoBehavior(blackhole: Blackhole): Unit = {
    blackhole.consume(coreLogger.sourceInfoBehavior(SLF4JLevel.INFO, line, file, enclosing))
  }
}
```

yields:

```
[info] Benchmark                               Mode  Cnt   Score   Error  Units
[info] CoreLoggerBenchmark.info                avgt    5  43.636 ± 0.059  ns/op
[info] CoreLoggerBenchmark.sourceInfoBehavior  avgt    5   5.233 ± 0.186  ns/op
[info] CoreLoggerBenchmark.trace               avgt    5   2.792 ± 0.427  ns/op
```

## Blindsight Logger

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

## Fluent Logger

The fluent logger is call by name, and so does not evaluate any messages or arguments unless `log()` is called and there is a successful predicate test.

```scala
class FluentBenchmark {
  val fluent: FluentLogger = LoggerFactory.getLogger.fluent

  @Benchmark
  def info(): Unit = {
    fluent.info.message("Hello world").log()
  }

  @Benchmark
  def infoWhen(): Unit = {
    fluent.info.when(false) { info =>
      info.message("Hello world").log()
    }
  }

  @Benchmark
  def trace(): Unit = {
    fluent.trace.message("Hello world").log()
  }

  @Benchmark
  def traceWhen(): Unit = {
    fluent.trace.when(false) { trace =>
      trace.message("Hello world").log()
    }
  }
}
```

Yields:

```scala
[info] Benchmark                  Mode  Cnt   Score   Error  Units
[info] FluentBenchmark.info       avgt    5  63.151 ± 2.089  ns/op
[info] FluentBenchmark.infoWhen   avgt    5   1.546 ± 0.056  ns/op
[info] FluentBenchmark.trace      avgt    5   4.872 ± 0.352  ns/op
[info] FluentBenchmark.traceWhen  avgt    5   1.535 ± 0.029  ns/op
```

The `info` benchmark is not all that different from the static builder, but the interesting thing is the `trace` benchmark.  It's still slower than a conditional check because it has to accumulate some state, but not by much.

Technically speaking, Logback can accept a logging event even calling `isTraceEnabled(markers)` returns false.  This is because the TurboFilter API is richer than the predicate API: you can return `FilterReply.ACCEPT` based on the message, parameter array, or Throwable.

```java
public abstract class TurboFilter extends ContextAwareBase implements LifeCycle {
    public abstract FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t);    
}
```

In practice, it's okay for markers to be the predicate testing option.

## Semantic Benchmarks

```
[info] Benchmark                    Mode  Cnt   Score   Error  Units
[info] SemanticBenchmark.info       avgt    5  73.584 ± 1.325  ns/op
[info] SemanticBenchmark.infoWhen   avgt    5   1.535 ± 0.031  ns/op
[info] SemanticBenchmark.trace      avgt    5  22.226 ± 9.561  ns/op
[info] SemanticBenchmark.traceWhen  avgt    5   1.530 ± 0.026  ns/op
```