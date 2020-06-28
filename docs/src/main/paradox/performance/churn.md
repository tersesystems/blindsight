# Memory Churn

## Overview

We're looking for memory churn.  

https://www.infoq.com/presentations/jvm-60-memory

https://www.slideshare.net/KirkPepperdine/trouble-with-memory

## Method Under 

Here's the method under benchmark.

```scala
class LoggingBenchmark {
  @Benchmark
  def infoWithStatement(): Unit = {
    logger.info(st"Hello world ${arg1}, ${arg2}, ${arg3}")
  }
}
```

We use 20 iterations of warmup and 10 iterations using `-prof gc`.

## Call By Value

A call by value statement looks like this:

```scala
class StrictSLF4JMethod {
  override def apply(st: Statement): Unit = {
     if (shouldLog) parameterList.executeStatement(st)
  }
}
```

```text
[info] Benchmark                                                        Mode  Cnt     Score     Error   Units
[info] LoggingBenchmark.infoWithStatement                               avgt   10   109.272 ±  41.589   ns/op
[info] LoggingBenchmark.infoWithStatement:·gc.alloc.rate                avgt   10  1541.414 ± 359.340  MB/sec
[info] LoggingBenchmark.infoWithStatement:·gc.alloc.rate.norm           avgt   10   256.000 ±   0.001    B/op
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Eden_Space       avgt   10  1542.856 ± 389.764  MB/sec
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Eden_Space.norm  avgt   10   255.809 ±  17.288    B/op
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Old_Gen          avgt   10     0.004 ±   0.002  MB/sec
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Old_Gen.norm     avgt   10     0.001 ±   0.001    B/op
[info] LoggingBenchmark.infoWithStatement:·gc.count                     avgt   10   111.000            counts
[info] LoggingBenchmark.infoWithStatement:·gc.time                      avgt   10   161.000                ms
```

Note that the normalized allocation rate is `256.000 ±   0.001    B/op` -- 256 bytes for every allocation.

for trace statements:

```text
[info] Benchmark                                                         Mode  Cnt     Score     Error   Units
[info] LoggingBenchmark.traceWithStatement                               avgt   10    26.187 ±   0.481   ns/op
[info] LoggingBenchmark.traceWithStatement:·gc.alloc.rate                avgt   10  3105.250 ±  58.260  MB/sec
[info] LoggingBenchmark.traceWithStatement:·gc.alloc.rate.norm           avgt   10   128.000 ±   0.001    B/op
[info] LoggingBenchmark.traceWithStatement:·gc.churn.G1_Eden_Space       avgt   10  3134.984 ± 332.544  MB/sec
[info] LoggingBenchmark.traceWithStatement:·gc.churn.G1_Eden_Space.norm  avgt   10   129.202 ±  12.911    B/op
[info] LoggingBenchmark.traceWithStatement:·gc.churn.G1_Old_Gen          avgt   10     0.003 ±   0.002  MB/sec
[info] LoggingBenchmark.traceWithStatement:·gc.churn.G1_Old_Gen.norm     avgt   10    ≈ 10⁻⁴              B/op
[info] LoggingBenchmark.traceWithStatement:·gc.count                     avgt   10    75.000            counts
[info] LoggingBenchmark.traceWithStatement:·gc.time                      avgt   10   161.000                ms
```

## Call By Name

A call by name statement is technically an anonymous function that gets invoked at the time of reference.  It is specified as ` => Statement`, meaning that there's a production of a `Statement` on invocation.

```scala
class StrictSLF4JMethod {
  override def apply(st: => Statement): Unit = {
     if (shouldLog) parameterList.executeStatement(st)
  }
}
```

For info statement:

```text
[info] Benchmark                                                        Mode  Cnt     Score     Error   Units
[info] LoggingBenchmark.infoWithStatement                               avgt   10   118.041 ±  57.481   ns/op
[info] LoggingBenchmark.infoWithStatement:·gc.alloc.rate                avgt   10  1815.783 ± 487.570  MB/sec
[info] LoggingBenchmark.infoWithStatement:·gc.alloc.rate.norm           avgt   10   320.000 ±   0.001    B/op
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Eden_Space       avgt   10  1818.187 ± 494.949  MB/sec
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Eden_Space.norm  avgt   10   320.872 ±  23.445    B/op
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Old_Gen          avgt   10     0.004 ±   0.004  MB/sec
[info] LoggingBenchmark.infoWithStatement:·gc.churn.G1_Old_Gen.norm     avgt   10     0.001 ±   0.001    B/op
[info] LoggingBenchmark.infoWithStatement:·gc.count                     avgt   10   101.000            counts
[info] LoggingBenchmark.infoWithStatement:·gc.time                      avgt   10   656.000                ms
```

Note that `320.000 ±   0.001    B/op` is called -- the call by name creates 320 bytes for every operation.

For trace statements, there's no execution of the statement at all, and there's no memory allocation.  It's completely flat.

```text
[info] Benchmark                                                Mode  Cnt   Score    Error   Units
[info] LoggingBenchmark.traceWithStatement                      avgt   10   3.946 ±  0.051   ns/op
[info] LoggingBenchmark.traceWithStatement:·gc.alloc.rate       avgt   10  ≈ 10⁻⁴           MB/sec
[info] LoggingBenchmark.traceWithStatement:·gc.alloc.rate.norm  avgt   10  ≈ 10⁻⁶             B/op
[info] LoggingBenchmark.traceWithStatement:·gc.count            avgt   10     ≈ 0           counts
```

## Conclusion

If you're logging operationally, at an INFO or higher level, then use call by value.  If you're logging diagnostic information, use call-by-value.

Practically speaking, this means using a `when` block.