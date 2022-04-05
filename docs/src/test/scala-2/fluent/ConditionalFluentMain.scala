/*
 * Copyright 2020 Terse Systems
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

package example.fluent

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.fluent.FluentLogger

import java.util.concurrent.atomic.AtomicBoolean

object ConditionalFluentMain {

  def main(args: Array[String]): Unit = {
    val latch = new AtomicBoolean(true)
    def test: Boolean = {
      // println("test called at " + System.currentTimeMillis())
      latch.getAndSet(!latch.get())
    }
    val logger: FluentLogger = LoggerFactory.getLogger(getClass).fluent.withCondition(test)

    logger.info
      .message("hello world, I render fine at")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()
    logger.info
      .message("hello world, I do not render at all at {}")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()
    logger.info
      .message("hello world, I render fine at")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()
    logger.info
      .message("hello world, I do not render at all at {}")
      .argument(System.currentTimeMillis().toString)
      .logWithPlaceholders()

    var counter: Int = 0
    def mod4 = {
      // println("mod4 called at " + System.currentTimeMillis())
      counter = (counter + 1) % 2
      counter == 1
    }

    val moreLogger = logger.withCondition(mod4)
    moreLogger.info.message("1 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("2 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("3 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("4 {}").argument(System.currentTimeMillis().toString).log()
    moreLogger.info.message("5 {}").argument(System.currentTimeMillis().toString).log()
  }
}
