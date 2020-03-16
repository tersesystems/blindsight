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
import com.tersesystems.blindsight.api.Statement
import com.tersesystems.blindsight.logstash.Implicits._

object FluentMain {
  private val fluent = LoggerFactory.getLogger.fluent

  def main(args: Array[String]): Unit = {
    val statement = Statement().withMessage("hello world")
    fluent.info(statement)

    fluent.info
      .marker("string" -> "steve")
      .marker("array" -> Seq("one", "two", "three"))
      .marker("number" -> 42)
      .marker("boolean" -> true)
      .message("herp")
      .message("derp")
      .message("{}")
      .argument("arg1" -> "value1")
      .message("{}")
      .argument("numericArg" -> 42)
      .message("and then some more text")
      .message("{}")
      .argument("booleanArg" -> false)
      .argument(Map("a" -> "b"))
      .argument("sequenceArg" -> Seq("a", "b", "c"))
      .log()
  }
}
