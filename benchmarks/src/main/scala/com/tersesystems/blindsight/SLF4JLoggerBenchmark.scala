/*
 * Copyright 2020 com.tersesystems.blindsight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class SLF4JLoggerBenchmark {

  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])

  val arg1 = ("one")
  val arg2 = ("two")
  val arg3 = ("three")

  @Benchmark
  def trace(): Unit = {
    slf4jLogger.trace("Hello world")
  }

  @Benchmark
  def traceWithArgs(): Unit = {
    slf4jLogger.trace("Hello world {}, {}, {}", arg1, arg2, arg3)
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
  def infoWithArgs(): Unit = {
    slf4jLogger.info("Hello world {}, {}, {}", arg1, arg2, arg3)
  }

  @Benchmark
  def isInfoEnabled(blackhole: Blackhole): Unit = {
    blackhole.consume(slf4jLogger.isInfoEnabled())
  }

}
