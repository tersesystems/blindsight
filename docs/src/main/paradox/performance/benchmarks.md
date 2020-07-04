# Benchmarks

This page gives a baseline for how much overhead Blindsight adds on top of using raw SLF4J.  This is purely CPU bound, so your results will vary depending on how powerful your CPU is.

If you want to run the benchmarks yourself, you can find them [here](https://github.com/tersesystems/blindsight/tree/master/benchmarks).  Please note that virtualized cloud hardware on EC2 or GCP can be very different from your laptop, so please be aware of your instance's CPU profile and budget.  It's best if you look at these numbers as a rough order of magnitude and as a comparison rather than an absolute statement of performance.
 
Note that the cost of logging is always vastly greater than adding conditional logic that controls logging, and is only partly incurred by the application itself: logs must be collected, indexed, and analyzed by the organization as a whole.  [Logging is free, logs are expensive](https://tersesystems.com/blog/2019/06/03/application-logging-in-java-part-6/).

Also be aware that JMH benchmarks can be tricky.  See [What's Wrong With My Benchmark Results? Studying Bad Practices in JMH Benchmarks](https://www.researchgate.net/publication/333825812_What%27s_Wrong_With_My_Benchmark_Results_Studying_Bad_Practices_in_JMH_Benchmarks) for details, and don't trust any benchmarks that don't show you the code.

## Hardware

Tests are run on a desktop CPU running Windows 10, using an Ubuntu image inside VirtualBox:

```text
Processor	Intel(R) Core(TM) i9-9900K CPU @ 3.60GHz, 3600 Mhz, 8 Core(s), 16 Logical Processor(s)
```

Tests were run on OpenJDK 11:

```
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.7+10)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.7+10, mixed mode
```

## JMH Visualizer

You can see the benchmarks in more detail at [JMH Visualizer](https://jmh.morethan.io/):

* [6/16](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200616T110241/openjdk11.json)
* [6/22](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200622T222138/openjdk11.json)
* [6/23](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200623T190617/openjdk11.json)
* [6/24](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200624T092631/openjdk11.json)
* [6/25](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200625T191639/openjdk11.json)
* [6/27](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200627T142114/openjdk11.json)

## Raw SLF4J Logger

Running SLF4J as a baseline gives an idea of the underlying SLF4J calls and how much overhead is added by Blindsight.

```scala
class SLF4JLoggerBenchmark {
  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])

  @Benchmark
  def trace(): Unit = {
    slf4jLogger.trace("Hello world")
  }

  @Benchmark
  def isTraceEnabled(blackhole: Blackhole): Unit = {
    blackhole.consume(slf4jLogger.isTraceEnabled())
  }

  @Benchmark
  def info(): Unit = {
    slf4jLogger.info("Hello world")
  }

  @Benchmark
  def isInfoEnabled(blackhole: Blackhole): Unit = {
    blackhole.consume(slf4jLogger.isInfoEnabled())
  }
}
```

With Logback 1.2.3 and a no-op appender:

```
[info] SLF4JLoggerBenchmark.info                  avgt    5   42.113 ±  1.356  ns/op
[info] SLF4JLoggerBenchmark.isInfoEnabled         avgt    5    4.745 ±  0.104  ns/op
[info] SLF4JLoggerBenchmark.isTraceEnabled        avgt    5    4.775 ±  0.157  ns/op
[info] SLF4JLoggerBenchmark.trace                 avgt    5    1.305 ±  0.024  ns/op
```

## Blindsight Logger

```scala
class LoggingBenchmark {

  val logger: Logger               = LoggerFactory.getLogger
  val condition: Condition         = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val infoConditionLogger: Logger  = logger.withCondition(condition)
  val neverConditionLogger: Logger = logger.withCondition(Condition.never)

  @Benchmark
  def trace(): Unit = {
    logger.trace("Hello world")
  }

  @Benchmark
  def traceWhen(): Unit = {
    logger.trace.when(condition) { log => log("Hello world") }
  }

  @Benchmark
  def traceCondition(): Unit = {
    infoConditionLogger.trace("Hello world")
  }

  @Benchmark
  def neverTrace(): Unit = {
    neverConditionLogger.trace("Hello world")
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
    logger.info.when(condition) { log => log("Hello world") }
  }

  @Benchmark
  def infoCondition(): Unit = {
    infoConditionLogger.info("Hello world")
  }

  @Benchmark
  def neverInfo(): Unit = {
    neverConditionLogger.info("Hello world")
  }

  @Benchmark
  def infoExplicitPredicate(): Unit = {
    if (logger.isInfoEnabled()) logger.info("Hello world")
  }
}
```

With Logback 1.2.3 and a no-op appender:

```
[info] LoggingBenchmark.info                      avgt    5   57.882 ±  1.290  ns/op
[info] LoggingBenchmark.infoCondition             avgt    5   93.005 ±  0.567  ns/op
[info] LoggingBenchmark.infoExplicitPredicate     avgt    5   54.650 ±  1.502  ns/op
[info] LoggingBenchmark.infoWhen                  avgt    5   63.091 ±  2.924  ns/op
[info] LoggingBenchmark.neverInfo                 avgt    5    2.808 ±  0.197  ns/op
[info] LoggingBenchmark.neverTrace                avgt    5    2.806 ±  0.138  ns/op
[info] LoggingBenchmark.trace                     avgt    5    4.297 ±  0.014  ns/op
[info] LoggingBenchmark.traceCondition            avgt    5   18.676 ±  0.946  ns/op
[info] LoggingBenchmark.traceExplicitPredicate    avgt    5    4.199 ±  0.188  ns/op
[info] LoggingBenchmark.traceWhen                 avgt    5    9.244 ±  0.140  ns/op
```

## Flow Logger

```scala

class FlowBenchmark {
  val flow        = LoggerFactory.getLogger.flow
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val neverLogger = flow.withCondition(Condition.never)

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
```

With Logback 1.2.3 and a no-op appender:

```
[info] FlowBenchmark.info                         avgt    5   47.409 ±  1.310  ns/op
[info] FlowBenchmark.infoWhen                     avgt    5  116.816 ±  3.505  ns/op
[info] FlowBenchmark.neverInfo                    avgt    5   39.095 ±  0.324  ns/op
[info] FlowBenchmark.neverTrace                   avgt    5   38.780 ±  0.610  ns/op
[info] FlowBenchmark.trace                        avgt    5   42.328 ±  1.771  ns/op
[info] FlowBenchmark.traceWhen                    avgt    5  114.078 ±  4.367  ns/op
```

The flow logger is slightly slower even when disabled because of `sourcecode.Args` creating a `Seq` of arguments.

## Fluent Logger

```scala

class FluentBenchmark {
  val fluent: FluentLogger = LoggerFactory.getLogger.fluent
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val neverConditionLogger = fluent.withCondition(Condition.never)

  @Benchmark
  def info(): Unit = {
    fluent.info.message("Hello world").log()
  }

  @Benchmark
  def infoWhen(): Unit = {
    fluent.info.when(condition) { info =>
      info.message("Hello world").log()
    }
  }

  @Benchmark
  def neverWhen(): Unit = {
    neverConditionLogger.info.message("Hello world").log()
  }

  @Benchmark
  def trace(): Unit = {
    fluent.trace.message("Hello world").log()
  }

  @Benchmark
  def neverTrace(): Unit = {
    neverConditionLogger.trace.message("Hello world").log()
  }

  @Benchmark
  def traceWhen(): Unit = {
    fluent.trace.when(condition) { trace =>
      trace.message("Hello world").log()
    }
  }

}
```

With Logback 1.2.3 and a no-op appender:

```
[info] FluentBenchmark.info                       avgt    5   90.699 ±  0.817  ns/op
[info] FluentBenchmark.infoWhen                   avgt    5   98.016 ±  3.917  ns/op
[info] FluentBenchmark.neverTrace                 avgt    5    4.383 ±  0.235  ns/op
[info] FluentBenchmark.neverWhen                  avgt    5    4.350 ±  0.042  ns/op
[info] FluentBenchmark.trace                      avgt    5    4.810 ±  0.038  ns/op
[info] FluentBenchmark.traceWhen                  avgt    5    8.010 ±  0.181  ns/op
```

## Semantic Logger

```scala

class SemanticBenchmark {
  val semantic      = LoggerFactory.getLogger.semantic[SampleMessage]
  val sampleMessage = SampleMessage("hello world")
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)

  @Benchmark
  def info(): Unit = {
    semantic.info(sampleMessage)
  }

  @Benchmark
  def infoWhen(): Unit = {
    semantic.info.when(condition) { info =>
      info(sampleMessage)
    }
  }

  @Benchmark
  def trace(): Unit = {
    semantic.trace(sampleMessage)
  }

  @Benchmark
  def traceWhen(): Unit = {
    semantic.trace.when(condition) { trace =>
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
```

With Logback 1.2.3 and a no-op appender:

```
[info] SemanticBenchmark.info                     avgt    5   76.736 ±  1.964  ns/op
[info] SemanticBenchmark.infoWhen                 avgt    5   78.773 ±  0.845  ns/op
[info] SemanticBenchmark.trace                    avgt    5   19.461 ±  1.099  ns/op
[info] SemanticBenchmark.traceWhen                avgt    5    8.012 ±  0.249  ns/op
```

## Unchecked Logger

```scala
class UncheckedBenchmark {
  val logger               = LoggerFactory.getLogger.unchecked
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val infoConditionLogger  = logger.withCondition(condition)
  val neverConditionLogger = logger.withCondition(Condition.never)

  val args = Arguments("one", "two", "three")

  @Benchmark
  def trace(): Unit = {
    logger.trace("Hello world")
  }

  @Benchmark
  def traceWithArg1(): Unit = {
    logger.trace("Hello world {}, {}", "one")
  }

  @Benchmark
  def traceWithArg1Arg2(): Unit = {
    logger.trace("Hello world {}, {}", "one", "two")
  }

  @Benchmark
  def traceWithArgs(): Unit = {
    logger.trace("Hello world {}, {}, {}", args)
  }

  @Benchmark
  def traceWhen(): Unit = {
    logger.trace.when(condition) { log => log("Hello world") }
  }

  @Benchmark
  def traceCondition(): Unit = {
    infoConditionLogger.trace("Hello world")
  }

  @Benchmark
  def neverTrace(): Unit = {
    neverConditionLogger.trace("Hello world")
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
  def infoWithArg1(): Unit = {
    logger.info("Hello world {}, {}", "one")
  }

  @Benchmark
  def infoWithArg1Arg2(): Unit = {
    logger.info("Hello world {}, {}", "one", "two")
  }

  @Benchmark
  def infoWithArgs(): Unit = {
    logger.info("Hello world {}, {}, {}", args)
  }

  @Benchmark
  def infoWhen(): Unit = {
    logger.info.when(condition) { log => log("Hello world") }
  }

  @Benchmark
  def infoCondition(): Unit = {
    infoConditionLogger.info("Hello world")
  }

  @Benchmark
  def neverInfo(): Unit = {
    neverConditionLogger.info("Hello world")
  }

  @Benchmark
  def infoExplicitPredicate(): Unit = {
     if (logger.isInfoEnabled()) logger.info("Hello world")
  }
}
```

With Logback 1.2.3 and a no-op appender:

```
[info] UncheckedBenchmark.info                    avgt    5   57.720 ±  5.151  ns/op
[info] UncheckedBenchmark.infoCondition           avgt    5   93.347 ±  2.561  ns/op
[info] UncheckedBenchmark.infoExplicitPredicate   avgt    5   60.161 ±  0.945  ns/op
[info] UncheckedBenchmark.infoWhen                avgt    5   62.670 ±  1.166  ns/op
[info] UncheckedBenchmark.infoWithArg1            avgt    5   79.083 ±  1.462  ns/op
[info] UncheckedBenchmark.infoWithArg1Arg2        avgt    5   68.226 ±  3.502  ns/op
[info] UncheckedBenchmark.infoWithArgs            avgt    5  100.909 ± 10.412  ns/op
[info] UncheckedBenchmark.neverInfo               avgt    5    2.301 ±  0.132  ns/op
[info] UncheckedBenchmark.neverTrace              avgt    5    2.277 ±  0.070  ns/op
[info] UncheckedBenchmark.trace                   avgt    5    3.574 ±  0.227  ns/op
[info] UncheckedBenchmark.traceCondition          avgt    5   18.149 ±  0.591  ns/op
[info] UncheckedBenchmark.traceExplicitPredicate  avgt    5    3.523 ±  0.089  ns/op
[info] UncheckedBenchmark.traceWhen               avgt    5    8.010 ±  0.137  ns/op
[info] UncheckedBenchmark.traceWithArg1           avgt    5    3.517 ±  0.042  ns/op
[info] UncheckedBenchmark.traceWithArg1Arg2       avgt    5    3.534 ±  0.061  ns/op
[info] UncheckedBenchmark.traceWithArgs           avgt    5    6.773 ±  0.568  ns/op
```
