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

import org.openjdk.jmh.annotations.{Benchmark, _}
import org.slf4j.event.{Level => SLF4JLevel}

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class SemanticBenchmark {
  val semantic             = LoggerFactory.getLogger.semantic[SampleMessage]
  val sampleMessage        = SampleMessage("hello world")
  val condition: Condition = Condition((level, _) => level.compareTo(SLF4JLevel.INFO) >= 0)

  @Benchmark
  def info(): Unit = {
    semantic.info(sampleMessage)
  }

  @Benchmark
  def infoWhen(): Unit = {
    semantic.info.when(condition) { info =>
      info(sampleMessage)
    }
  }

  @Benchmark
  def trace(): Unit = {
    semantic.trace(sampleMessage)
  }

  @Benchmark
  def traceWhen(): Unit = {
    semantic.trace.when(condition) { trace =>
      trace(sampleMessage)
    }
  }

}

final case class SampleMessage(messageType: String)

object SampleMessage {
  implicit val toStatement: ToStatement[SampleMessage] = ToStatement { sample =>
    Message(sample.messageType).toStatement
  }
}
