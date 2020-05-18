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

import com.tersesystems.blindsight._

object FluentMain {
  private val fluent = LoggerFactory.getLogger.fluent

  def main(args: Array[String]): Unit = {
    val statement = Statement().withMessage("hello world")
    fluent.info(statement)

    import com.tersesystems.blindsight.AST._
    import com.tersesystems.blindsight.DSL._
    import com.tersesystems.blindsight.logstash.Implicits._
    fluent.info
      .marker(bodj("string" -> "steve"))
      .marker(bodj("array" -> Seq("one", "two", "three")))
      .marker(bodj("number" -> 42))
      .marker(bodj("boolean" -> true))
      .message("herp")
      .message("derp")
      .message("{}")
      .argument(bodj("arg1" -> "value1"))
      .message("{}")
      .argument(bodj("numericArg" -> 42))
      .message("and then some more text")
      .message("{}")
      .argument(bodj("booleanArg" -> false))
      .argument(bodj("a" -> "b"))
      .argument(bodj("sequenceArg" -> Seq("a", "b", "c")))
      .log()
  }
}

object FluentSimple {
  def main(args: Array[String]): Unit = {

    // #fluent-logger
    import com.tersesystems.blindsight.fluent.FluentLogger

    val logger                     = com.tersesystems.blindsight.LoggerFactory.getLogger(getClass)
    val fluentLogger: FluentLogger = logger.fluent
    // #fluent-logger

    // #fluent-markers
    import net.logstash.logback.marker.{Markers => LogstashMarkers}
    import org.slf4j.Marker
    val someMarker: Marker = LogstashMarkers.append("user", "will")

    fluentLogger.info.marker(someMarker).log()
    // #fluent-markers

    val args: Argument = Argument("some arguments")
    val exception      = new RuntimeException("ex")

    // #fluent-statement
    fluentLogger.info
      .marker(someMarker)
      .message("some message {}")
      .argument(args)
      .message("exception {}")
      .cause(exception)
      .log()
    // #fluent-statement
  }
}
