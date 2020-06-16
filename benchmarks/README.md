## Benchmarks

Using https://github.com/plokhotnyuk/jsoniter-scala#run-jvm-benchmarks as a guide (incredibly useful, way more stuff in there)

## Running 

To run as json:

```
sbt 'benchmarks/jmh:run -rf json'
LOGDATE=$(date +%Y%m%dT%H%M%S)
mkdir -p $LOGDATE
mv benchmarks/jmh-result.json benchmarks/$LOGDATE/openjdk11.json
```

You can add a link to [JMH Visualizer](https://jmh.morethan.io/):

https://jmh.morethan.io/?source=https://raw.githubusercontent.com/tersesystems/blindsight/master/benchmarks/results/20200616T110241/openjdk11.json

## Profiling

To list profiling tools:

```
sbt 'benchmarks/jmh:run -lprof'
```

Best is async-profiler, use it for flame graphs.

## JIT Assembly

Profiling will not give you the results of escape analysis, so if an instanstiation is optimized out of existance, the profiler won't be able to see it (adding the profiler breaks escape analysis optimizations).  So the only real way to see it is through seeing the assembly code produced by JIT compiler.

```
sbt 'benchmarks/jmh:run -prof perfasm -wi 10 -i 10 LoggingBenchmark.info'
```

There's a tool called [JITWatch](https://github.com/AdoptOpenJDK/jitwatch/wiki) that makes this less painful by processing the logs for you.

> **NOTE**: there does not seem to be a way to make perfasm produce **anything** under Virtualbox.
