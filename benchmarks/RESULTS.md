# Results

On the following hardware:

```text
System Model	XPS 15 7590
Processor	Intel(R) Core(TM) i7-9750H CPU @ 2.60GHz, 2592 Mhz, 6 Core(s), 12 Logical Processor(s)
```

Using a no-op appender and Logback:

```text
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark                                        Mode  Cnt    Score    Error  Units
[info] LoggingBenchmark.conditionalTraceBenchmark       avgt    5  622.194 ∩┐╜ 59.861  ns/op
[info] LoggingBenchmark.falseConditionalTraceBenchmark  avgt    5  581.672 ∩┐╜ 83.716  ns/op
[info] LoggingBenchmark.ifTraceBenchmark                avgt    5    5.054 ∩┐╜  0.081  ns/op
[info] LoggingBenchmark.traceBenchmark                  avgt    5  595.168 ∩┐╜ 60.056  ns/op
[info] LoggingBenchmark.traceWhenBenchmark              avgt    5    2.596 ∩┐╜  0.120  ns/op
[success] Total time: 55 s, completed Jun 9, 2020, 8:47:45 PM
```