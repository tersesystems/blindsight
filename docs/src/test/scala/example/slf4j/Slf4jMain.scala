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

package example.slf4j

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

import com.tersesystems.blindsight._
import net.logstash.logback.marker.{Markers => LogstashMarkers}
import org.slf4j.MarkerFactory
import org.slf4j.event.Level

object Slf4jMain {

  private val buffer = EventBuffer(50)

  private val logger = LoggerFactory
    .getLogger(getClass)
    .withEntryTransform(e => {
      val message = e.message + " IN BED"
      e.copy(message = message)
    })
    .withEventBuffer(Level.DEBUG, buffer)
    .withEventBuffer(Level.TRACE, buffer)

  final case class FeatureFlag(flagName: String)

  object FeatureFlag {
    implicit val toMarkers: ToMarkers[FeatureFlag] = ToMarkers { instance =>
      Markers(MarkerFactory.getDetachedMarker(instance.flagName))
    }
  }

  case class CreditCard(number: String)

  def main(args: Array[String]): Unit = {
    val featureFlag = FeatureFlag("flag.enabled")
    if (logger.isDebugEnabled(featureFlag)) {
      logger.debug("this is a test")
    }

    val marker = MarkerFactory.getDetachedMarker("foo")
    logger.info(Markers(marker), "hello")

    // Cannot use ToMarkers here
    import MarkersEnrichment._
    logger.debug(featureFlag.asMarkers, "markers must be explicit here to prevent API confusion")

    logger.trace { trace =>
      trace("A trace statement can be written lazy style")
    }

    logger.info.when(System.currentTimeMillis() % 2 == 0) { log => log("I am divisable by two") }

    logger.info("hello world")

    val m1 = MarkerFactory.getMarker("M1")

    logger.info("this is strict {} {}", 42, 53)
    logger.info("arg {}, arg {}, arg 3 {}", Arguments(1, "2", false))

    val e = new Exception("derp")
    logger.error("this is an error", e)
    logger.error("this is an error with argument {}", "arg1", e)
    logger.error(
      "this is an error with two arguments {} {}",
      "arg1",
      "arg2"
    )
    //logger.info("won't compile, must define ToArguments[CreditCard]", creditCard)

    logger.info(
      Markers(LogstashMarkers.append("key", "value")),
      "marker and argument {}",
      "argumentKey=argumentValue"
    )

    implicit val dateToArgument: ToArgument[Date] = ToArgument[java.util.Date] { date =>
      new Argument(DateTimeFormatter.ISO_INSTANT.format(date.toInstant))
    }

    implicit val instantToArgument: ToArgument[java.time.Instant] =
      ToArgument[java.time.Instant] { instant =>
        new Argument(DateTimeFormatter.ISO_INSTANT.format(instant))
      }

    logger.info("date is {}", new java.util.Date())
    logger.info("instant is {}", Instant.now())

    val m2                = MarkerFactory.getMarker("M2")
    val loggerWithMarkers = logger.withMarker(m1).withMarker(m2)
    loggerWithMarkers.info("I should have two markers")

    println(s"There are ${buffer.size} entries in the buffer")
    buffer.take(buffer.size).foreach { event =>
      println(s"event $event")
    }
  }
}
