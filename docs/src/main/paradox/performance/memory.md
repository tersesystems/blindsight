# Memory Usage

Kirk Pepperdine gives a very interesting presentation called **The Trouble with Memory** ( [transcript](https://www.infoq.com/presentations/memory-jvm/) / [video](https://www.youtube.com/watch?t=549&v=mfS-P49FSbY&feature=youtu.be) / [slides](https://qconsf.com/system/files/presentation-slides/trouble_with_memory.pdf) ), which discusses "memory churn" -- excessive amounts of allocation and release of heap in the JVM.   The performance impact of memory is not well-known, but according to Pepperdine it can have severe effects on applications, while being very difficult to spot.

One anecdote stood out in particular:

> So I gave my usual logging rant at a workshop that I gave. I was in the Netherlands about a year or so ago. And that night, they went and stripped all the logging out of their transactional monitoring framework that they're using, which wasn't getting them the performance they wanted, which is why I was there giving the workshop in the first place. And when they stripped out all their logging, the throughput jumped by a factor of four. And they're going like, "Oh, so now our performance problem not only went away, we've been tuning this thing so long that actually, when they got rid of the real problem, it went much faster than they needed, like twice as fast as what they needed to go."

So it's clear that logging can play a part in memory allocation pressure.  If there is too much memory allocation, then the tenure system may suffer from premature promotion, and short-lived objects may be part of a major GC, which will hurt throughput and cause longer GC pauses.

Pepperdine is clear that each operation may only allocate a small number of bytes, but can be called at such high rates that it impacts the JVM:

> You get all of these funny downstream costs that you don't even think about. In terms of the allocation, it's still quick. If the objects die very quickly, there's zero cost to collect them, so that's true. That's what garbage collection people have been telling you all the time, "Go, don't worry about it. Just create objects. It's free to collect them." It may be free to collect them, but **quick times a large number does equal slow**. If you have high creation rates, it's not free to create. It may be free to collect, but it's not free to create at the higher rate.

If you want a general look and feel, allocation pressure can be viewed from [Java Mission Control](http://hirt.se/blog/?p=381), which will show the inside TLAB allocations.  (Note that it was [removed](https://stackoverflow.com/questions/51274072/how-to-get-allocation-pressure-in-java-mission-control-that-ships-with-java-10) and then added back in later version of JMC, so you may want to download a later version.)  The ideal allocation rate is below 300 MB/second, but generally anything under 1 GB/second is fine -- [Frugal Memory Management on the JVM](https://srvaroa.github.io/assets/frugal_memory_management_on_the_jvm.pdf) is another good presentatation on the topic.

The question is how much churn logging can produce, and how to best prevent it.  

## Schools of Performant Logging

There are two schools of thought when it comes to logging efficiently: explicit guard statements vs lazy evaluation.

The first approach says that logging should be done up front inside a guard statement, and all computation and memory will be inside the block:

```scala
if (logger.isLoggingInfo()) {
  val arg1 = ...
  val arg2 = ...
  val arg3 = ...
  val arguments = Arguments(arg1, arg2, arg3)
  logger.info("My message with {} {} {}", arguments)
}
```

The issue with guard statements is that they don't flow very well, and it's easy to forget them.  There's also the argument that if you're logging at info level, you're checking the guard twice (since it gets checked in `logger.info` itself), and so you may as well eliminate the condition altogether. 

The second approach says that logging should be done inside a function block, with lazy evaluation:

```scala
logger.info { info =>
  val arg1 = ...
  val arg2 = ...
  val arg3 = ...
  val arguments = Arguments(arg1, arg2, arg3)
  info("My message with {} {} {}", arguments)
}
```

The issue with lazy evaluation is that in situations where logging should happen, the function itself causes extra allocation and so may be simply adding to the memory allocation pressure.

## Testing with JMH

JMH lets you benchmark for [GC allocation pressure](https://shipilev.net/blog/2016/arrays-wisdom-ancients/#_not_an_allocation_pressure) using the `-prof gc` option.  This provides some nice options for JMH, notably the following metrics:

* `gc.alloc.rate`
* `gc.alloc.rate.norm`
* `gc.churn.PS_Eden_Space`
* `gc.churn.PS_Eden_Space.norm`
* `gc.churn.PS_Survivor_Space`
* `gc.churn.PS_Survivor_Space.norm`
* `gc.count`
* `gc.time`

### JMH Benchmarks

The details of the JMH Benchmark are [here](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200627T142114/openjdk11.json).

There are two methods under benchmark here, one which logs state logging

There's [`LoggingBenchmark.infoWithArgs`](https://github.com/tersesystems/blindsight/blob/master/benchmarks/src/main/scala/com/tersesystems/blindsight/LoggingBenchmark.scala#L81):

```scala
@Benchmark
def infoWithArgs(): Unit = {
  logger.info("Hello world {}, {}, {}", args)
}
```

and [`LoggingBenchmark.lazyInfoWithArgs`](https://github.com/tersesystems/blindsight/blob/master/benchmarks/src/main/scala/com/tersesystems/blindsight/LoggingBenchmark.scala#L91):

```scala
@Benchmark
def lazyInfoWithArgs(): Unit = {
  logger.info { log =>
    log("Hello world {}, {}, {}", args)
  }
}
```

For the sake of completeness, there's also the trace variants, [`LoggingBenchmark.traceWithArgs`](https://github.com/tersesystems/blindsight/blob/master/benchmarks/src/main/scala/com/tersesystems/blindsight/LoggingBenchmark.scala#L32) and [`LoggingBenchmark.lazyTraceWithArgs`](https://github.com/tersesystems/blindsight/blob/master/benchmarks/src/main/scala/com/tersesystems/blindsight/LoggingBenchmark.scala#L42).  These are the same style, but using a logging level which is disabled and so should produce no output.

## Results

Looking at `infoWithArgs`, we see the following GC rates:

* `gc.alloc.rate` - 751 MB/second
* `gc.alloc.rate.norm` - 80 B/second
* `gc.churn.G1_Eden_Space` - 801 MB/second
* `gc.churn.G1_Eden_Space.norm` - 80 B/second
* `gc.churn.G1_Old_Gen` - ~ 0
* `gc.churn.G1_Old_Gen.norm` - ~ 0 
* `gc.count` - 52 
* `gc.time` - 26 ms

So far so good.  We can see that we're less than 1GB/second and very few instances are making it into Old Gen space.  On average, we're allocating 80 bytes per invocation, although this is a derived value and has some edge cases.

Now let's look at `lazyInfoWithArgs`:

* `gc.alloc.rate` - 1065 MB/second
* `gc.alloc.rate.norm` - 152 B/second
* `gc.churn.G1_Eden_Space` - 1066 MB/second
* `gc.churn.G1_Eden_Space.norm` - 152 B/second
* `gc.churn.G1_Old_Gen` - ~ 0
* `gc.churn.G1_Old_Gen.norm` - ~ 0 
* `gc.count` - 51 
* `gc.time` - 36 ms

Running the exact same method inside a block raises the allocation rate from 751 MB/s to 1065 MB/s, and kicks up the eden churn rate from 801 MB/s to 1066 MB/s. 

Now let's look at the `traceWithArgs` and `lazyTraceWithArgs` benchmarks:

`traceWithArgs`:

* `gc.alloc.rate` - 0
* `gc.alloc.rate.norm` - 0
* `gc.churn.G1_Eden_Space` - 0
* `gc.churn.G1_Eden_Space.norm` - 0
* `gc.churn.G1_Old_Gen` - ~ 0
* `gc.churn.G1_Old_Gen.norm` - ~ 0 
* `gc.count` - 0 
* `gc.time` - 0

`lazyTraceWithArgs`:

* `gc.alloc.rate` - 0
* `gc.alloc.rate.norm` - 0
* `gc.churn.G1_Eden_Space` - 0
* `gc.churn.G1_Eden_Space.norm` - 0
* `gc.churn.G1_Old_Gen` - ~ 0
* `gc.churn.G1_Old_Gen.norm` - ~ 0 
* `gc.count` - 0
* `gc.time` - 0 ms

In both scenarios, if there's no allocation inside the benchmark itself -- there's no GC.  At all.

However, this isn't the full story.  When memory is allocated inside a benchmark which takes 4 ns normally, it quickly becomes the bottleneck.  For example, you can see that `gc.alloc.rate` for `traceWithStatement` is 3,036 MB/second!  So if there's no guard or lazy block managing diagnostic logging, it will quickly become an issue.

## Conclusions

For operational logging -- statements that are `INFO`, `WARN`, or `ERROR` -- the expectation is that creating a logging statement will result in a log entry.  Simply calling `logger.info` is enough:

```scala
// event that is always logged once per request on completion
logger.info(markers, "Request served with {}", response)
```

Using a block in this situation will add extra memory allocation with no benefit.

In @ref:[conditional logging](../usage/conditional.md) scenarios, where sampling or circuit breakers may prevent logging, you should use the lazy form:

```scala
logger.withCondition(condition).info { info =>
  val sampledArgs = ...
  info("This may be sampled {}", sampledArgs)
}
```

For diagnostic logging, you should likewise always wrap allocations and use the lazy form:

```scala
logger.trace { trace =>
  val debugInfo = ...
  trace(st"This can be an expensive ${response} with ${debugInfo} information")
}
```
