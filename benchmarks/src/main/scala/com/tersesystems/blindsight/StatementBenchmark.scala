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
class StatementBenchmark {

  //    @Benchmark
  //    def statementFromInterpolation(blackhole: Blackhole): Unit = {
  //      val arg1 = "one"
  //      val arg2 = "two"
  //      val arg3 = "three"
  //      blackhole.consume(st"Hello world $arg1, $arg2, $arg3")
  //    }

  @Benchmark
  def statementFromApply(blackhole: Blackhole): Unit = {
    val arg1 = "one"
    val arg2 = "two"
    val arg3 = "three"
    blackhole.consume(
      Statement(message = "Hello world {}, {}, {}", arguments = Arguments(arg1, arg2, arg3))
    )
  }

  @Benchmark
  def statementFromArray(blackhole: Blackhole): Unit = {
    val arg1 = "one"
    val arg2 = "two"
    val arg3 = "three"
    blackhole.consume(
      Statement(
        message = "Hello world {}, {}, {}",
        arguments = Arguments.fromArray(Array(Argument(arg1), Argument(arg2), Argument(arg3)))
      )
    )
  }

}
