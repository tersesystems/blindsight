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
import com.tersesystems.blindsight.api.{Argument, Arguments, Markers, Statement, ToArgument}
import com.tersesystems.blindsight.flow.FlowBehavior.Source
import com.tersesystems.blindsight.flow.{FlowBehavior, FlowMethod}
import com.tersesystems.blindsight.logstash.Implicits._
import com.tersesystems.logback.tracing.{SpanInfo, SpanMarkerFactory}
import com.tersesystems.logback.uniqueid.RandomUUIDIdGenerator
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.event.Level
import sourcecode.Enclosing

import scala.collection.mutable
import scala.compat.java8.FunctionConverters.{asJavaFunction, asJavaSupplier}

/**
 * This class demonstrates how to do hierarchical parent/child span style tracing to a
 * service using the flow logger API.  This also shows duration of the spans.
 *
 * Most of the output will show up in application.json, so look there.
 */
// #honeycomb_flow_example
object HoneycombFlow {

  private val logger = LoggerFactory.getLogger

  private val idgen = new RandomUUIDIdGenerator
  private val builder: SpanInfo.Builder = {
    SpanInfo
      .builder()
      .setServiceName("blindsight-example")
  }

  implicit def flowMapping[B: ToArgument](
      implicit spanInfo: SpanInfo
  ): HoneycombFlowBehavior[B] = {
    new HoneycombFlowBehavior[B]
  }

  def main(args: Array[String]): Unit = {
    generateRootSpan { implicit rootSpan =>
      val intResult    = flowMethod(1, 2)
      val personResult = personFlowMethod(1, 2)
    }
  }

  def flowMethod(arg1: Int, arg2: Int)(implicit spanInfo: SpanInfo): Int = {
    logger.flow.info {
      arg1 + arg2
    }
  }

  def personFlowMethod(arg1: Int, arg2: Int)(implicit spanInfo: SpanInfo): Person = {
    logger.flow.info {
      Person(name = "Will", age = arg1 + arg2)
    }
  }

  private def generateRootSpan[T](block: SpanInfo => T)(implicit enclosing: Enclosing): T = {
    val rootSpan =
      builder.setRootSpan(asJavaSupplier(() => idgen.generateId()), enclosing.value).buildNow
    try {
      block(rootSpan)
    } finally {
      // The root span has to be logged _last_, after the child spans.
      logger.info(
        HoneycombFlowBehavior.markerFactory(rootSpan),
        s"${enclosing.value} exit, duration ${rootSpan.duration()}"
      )
    }
  }

  case class Person(name: String, age: Int)

  object Person {
    implicit val personToArguments: ToArgument[Person] = ToArgument { person =>
      Argument("person" -> Map("name" -> Argument(person.name), "age" -> Argument(person.age)))
    }
  }
}

class HoneycombFlowBehavior[B: ToArgument](implicit spanInfo: SpanInfo) extends FlowBehavior[B] {
  import HoneycombFlowBehavior.markerFactory

  // Create a thread local stack of span info.
  private val threadLocalStack: ThreadLocal[mutable.Stack[SpanInfo]] = {
    val local: ThreadLocal[mutable.Stack[SpanInfo]] = new ThreadLocal()
    local.set(mutable.Stack[SpanInfo]())
    local
  }

  override def entryStatement(source: Source): Option[Statement] = {
    // Start a child span, and push it onto the stack
    spanInfo.withChild(source.enclosing.value, asJavaFunction(pushCurrentSpan))
    None
  }

  override def throwingStatement(throwable: Throwable, source: Source): Option[(Level, Statement)] =
    Some {
      (
        Level.ERROR,
        Statement()
          .withThrowable(throwable)
          .withMarkers(Markers(markerFactory(popCurrentSpan)))
          .withMessage(s"${source.enclosing.value} exception")
      )
    }

  override def exitStatement(resultValue: B, source: Source): Option[Statement] = Some {
    val span = popCurrentSpan
    Statement()
      .withMarkers(Markers(markerFactory(span)))
      .withMessage(s"${source.enclosing.value} exit, duration ${span.duration()}")
      .withArguments(Arguments(resultValue))
  }

  private def pushCurrentSpan(spanInfo: SpanInfo): Unit = threadLocalStack.get.push(spanInfo)
  private def popCurrentSpan: SpanInfo                  = threadLocalStack.get().pop()
}

object HoneycombFlowBehavior {
  val markerFactory = new SpanMarkerFactory
}
// #honeycomb_flow_example
