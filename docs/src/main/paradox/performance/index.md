@@@ index

* [Benchmarks](benchmarks.md)
* [Memory Usage](memory.md)

@@@

# Performance

Blindsight has a full set of JMH benchmarks that cover creating statements, adding markers, and using conditional and contextual logging.

The high level summary is that most operations in Blindsight take less than 100 nanoseconds.  It does this through a combination of optimizations.
 
First, Blindsight makes heavy use of [value classes](https://docs.scala-lang.org/overviews/core/value-classes.html), so that types only exist at compile time.

Second, Blindsight leverages the [inlining optimizer](https://www.lightbend.com/blog/scala-inliner-optimizer) built into Scala 2.12 to inline methods where possible.

Third, Blindsight makes use of macros to optimize inconvenient cases, such as hetrogenous types in `Arguments` and statement interpolation.

Fourth, Blindsight short-circuits conditional logging when `Condition.never` is seen in the chain. 
 
Fifth, Blindsight ensures that features that may cause additional overhead, such as source code markers, are disabled by default. 

## Memory Allocation

There is a section that discusses @ref:[memory usage](memory.md) in depth using JMH benchmarks and profilers.

## Execution Time

There is a section that discusses the total time elapsed for each operation in @ref:[benchmarks](benchmarks.md) in depth using JMH.
