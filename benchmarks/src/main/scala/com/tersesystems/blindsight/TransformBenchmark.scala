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

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class TransformBenchmark {

  private val b50    = EventBuffer(50)
  private val b50000 = EventBuffer(50000)

  private val transformF: (Entry => Entry) = { e =>
    e.copy(message = e.message + " IN BED")
  }

  val logger = LoggerFactory.getLogger

  val transformLogger: Logger = logger.withEntryTransform(transformF)

  val buffer50Logger: Logger = logger.withEventBuffer(b50)

  val buffer50000Logger: Logger = logger.withEventBuffer(b50000)

  @Benchmark
  def transform(): Unit = {
    transformLogger.info("Hello world")
  }

  @Benchmark
  def buffer50(): Unit = {
    buffer50Logger.info("Hello world")
  }

  @Benchmark
  def buffer50000(): Unit = {
    buffer50000Logger.info("Hello world")
  }

}
