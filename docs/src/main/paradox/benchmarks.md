# Results

This page gives a baseline for how much overhead Blindsight adds on top of using raw SLF4J.  This is purely CPU bound, so your results will vary depending on how powerful your CPU is.

If you want to run the benchmarks yourself, you can find them [here](https://github.com/tersesystems/blindsight/tree/master/benchmarks).  Please note that virtualized cloud hardware on EC2 or GCP can be very different from your laptop, so please be aware of your instance's CPU profile and budget.

## Hardware

Tests are run on a desktop CPU running Windows 10, using an Ubuntu image inside VirtualBox:

```text
Processor	Intel(R) Core(TM) i9-9900K CPU @ 3.60GHz, 3600 Mhz, 8 Core(s), 16 Logical Processor(s)
```

## Raw SLF4J Logger

Running SLF4J as a baseline with Logback 1.2.3 and a no-op appender:

## Blindsight Logger


## Flow Logger


## Fluent Logger



## Semantic Logger
