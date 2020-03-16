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

package example.flow

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.flow.FlowBehavior.Source
import com.tersesystems.blindsight.flow._
import com.tersesystems.blindsight.logstash.Implicits._
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.event.Level
import org.slf4j.{Marker, MarkerFactory}

/**
 * This class demonstrates xlogger-style entry/exit tracing using the flow API.
 */
object FlowMain {
  private val logger = LoggerFactory.getLogger

  private implicit def flowBehavior[B: ToArguments]: FlowBehavior[B] =
    new XLoggerFlowBehavior[B]

  def main(args: Array[String]): Unit = {
    logger.info("About to execute number flow")
    val intResult = flowMethod(1, 2)
    println("This is " + intResult)

    logger.info("About to execute person flow")
    val personResult = personFlowMethod(1, 2)
    println("This is " + personResult)
  }

  def flowMethod(arg1: Int, arg2: Int): Int = logger.flow.trace {
    arg1 + arg2
  }

  def personFlowMethod(arg1: Int, arg2: Int): Person = logger.flow.trace {
    Person(name = "Will", age = arg1 + arg2)
  }

  case class Person(name: String, age: Int)

  object Person {
    implicit val personToArguments: ToArguments[Person] = ToArguments { person =>
      Arguments(kv("person", person.name), kv("age", person.age))
    }
  }
}

/**
 * A flow behavior that implements the <a href="http://www.slf4j.org/extensions.html#extended_logger">extended logger</a> interface.
 *
 * @see https://github.com/qos-ch/slf4j/blob/master/slf4j-ext/src/main/java/org/slf4j/ext/XLogger.java
 *
 * @tparam B the return type.
 */
class XLoggerFlowBehavior[B: ToArguments] extends FlowBehavior[B] {
  override def entryMarkers(source: Source): Markers = XLoggerFlowBehavior.entryMarkers
  override def exitMarkers(source: Source): Markers  = XLoggerFlowBehavior.exitMarkers

  override def entryStatement(source: Source): Option[Statement] = {
    import com.tersesystems.blindsight.logstash.ToArgumentsImplicits._

    Some(
      Statement()
        .withMarkers(entryMarkers(source))
        .withMessage(s"${source.enclosing.value} entry {}")
        .withArguments(source.args)
    )
  }

  override def throwingStatement(
      throwable: Throwable,
      source: Source
  ): Option[(Level, Statement)] = {
    Some(
      Level.ERROR, // xlogger logs exceptions at an error level.
      Statement()
        .withThrowable(throwable)
        .withMarkers(XLoggerFlowBehavior.throwingMarkers)
        .withMessage(s"${source.enclosing.value} exception")
    )
  }

  override def exitStatement(resultValue: B, source: Source): Option[Statement] = {
    Some(
      Statement()
        .withMarkers(exitMarkers(source))
        .withMessage(s"${source.enclosing.value} exit with result {}")
        .withArguments(resultValue)
    )
  }
}

object XLoggerFlowBehavior {
  // markers are same as XLogger
  val flowMarker: Marker = MarkerFactory.getMarker("FLOW")
  val entryMarker: Marker = {
    val entry = MarkerFactory.getMarker("ENTRY")
    entry.add(flowMarker)
    entry
  }
  val exitMarker: Marker = {
    val exit = MarkerFactory.getMarker("EXIT")
    exit.add(flowMarker)
    exit
  }
  val exceptionMarker: Marker = MarkerFactory.getMarker("EXCEPTION")
  val throwingMarker: Marker = {
    val throwing = MarkerFactory.getMarker("THROWING")
    throwing.add(exceptionMarker)
    throwing
  }

  val flowMarkers: Markers     = Markers(flowMarker)
  val entryMarkers: Markers    = Markers(entryMarker)
  val exitMarkers: Markers     = Markers(exitMarker)
  val throwingMarkers: Markers = Markers(throwingMarker)

}
