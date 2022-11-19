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

import com.tersesystems.blindsight.flow.FlowBehavior
import org.openjdk.jmh.annotations.{Benchmark, _}
import org.slf4j.event.{Level => SLF4JLevel}

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class FlowBenchmark {
  val flow                 = LoggerFactory.getLogger.flow
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)
  val neverLogger          = flow.withCondition(Condition.never)

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
