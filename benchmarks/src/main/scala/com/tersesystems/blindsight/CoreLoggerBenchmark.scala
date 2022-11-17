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

import com.tersesystems.blindsight.core.CoreLogger
import org.openjdk.jmh.annotations._
import org.slf4j.event.{Level => SLF4JLevel}
import sourcecode.{Enclosing, File, Line}

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class CoreLoggerBenchmark {

  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(classOf[CoreLoggerBenchmark])
  private val coreLogger  = CoreLogger(slf4jLogger)
  val line: Line          = new Line(42)
  val file: File          = new File("file")
  val enclosing           = new Enclosing("enclosing")

  val arg1 = Argument("one")
  val arg2 = Argument("two")
  val arg3 = Argument("three")
  val args = Arguments(arg1, arg2, arg3)

  @Benchmark
  def trace(): Unit = {
    // 2.715 ns for trace
    coreLogger.parameterList(SLF4JLevel.TRACE).message("Hello world")
  }

  @Benchmark
  def traceWithStatement(): Unit = {
    coreLogger
      .parameterList(SLF4JLevel.TRACE)
      .executeStatement(Statement("Hello world {}, {}, {}", Arguments(arg1, arg2, arg3)))
  }

  @Benchmark
  def info(): Unit = {
    coreLogger.parameterList(SLF4JLevel.INFO).message("Hello world")
  }

  @Benchmark
  def infoWithStatement(): Unit = {
    coreLogger
      .parameterList(SLF4JLevel.INFO)
      .executeStatement(Statement("Hello world {}, {}, {}", Arguments(arg1, arg2, arg3)))
  }

}
