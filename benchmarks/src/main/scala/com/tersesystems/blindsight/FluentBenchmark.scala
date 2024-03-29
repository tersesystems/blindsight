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

import com.tersesystems.blindsight.fluent.FluentLogger
import org.openjdk.jmh.annotations.{Benchmark, _}
import org.slf4j.event.{Level => SLF4JLevel}

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class FluentBenchmark {
  val fluent: FluentLogger = LoggerFactory.getLogger.fluent
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val neverConditionLogger = fluent.withCondition(Condition.never)

  val arg1 = Argument("one")
  val arg2 = Argument("two")
  val arg3 = Argument("three")

  @Benchmark
  def info(): Unit = {
    fluent.info.message("Hello world").log()
  }

  @Benchmark
  def infoWithArgs(): Unit = {
    fluent.info
      .statement(Statement("Hello world {}, {}, {}"))
      .argument(arg1)
      .argument(arg2)
      .argument(arg3)
      .log()
  }

  @Benchmark
  def infoWithStatement(): Unit = {
    fluent.info.statement(Statement("Hello world {}, {}, {}", Arguments(arg1, arg2, arg3))).log()
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
