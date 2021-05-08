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

import com.tersesystems.blindsight._
import org.slf4j.MarkerFactory
import org.slf4j.event.Level

object ConditionalMain {

  def main(args: Array[String]): Unit = {
    val buffer = EventBuffer(50)
    val condition = Condition { (level: Level, markers: Markers) =>
      level != Level.WARN
    }
    val logger = LoggerFactory.getLogger(getClass).withCondition(condition).withEventBuffer(buffer)

    val e = new RuntimeException
    logger.info("hello world, I render fine at {}", System.currentTimeMillis())
    logger.warn("hello world, I do not render at all at {}", System.currentTimeMillis())
    logger.info("hello world, I render fine at {}", System.currentTimeMillis())
    logger.warn("hello world, I do not render at all at {}", System.currentTimeMillis())

    val excludeMarker = Markers(MarkerFactory.getMarker("EXCLUDE"))
    val whenCondition = Condition { (markers: Markers) =>
      markers != excludeMarker
    }

    // don't include the marker
    logger.info.when(whenCondition) { info =>
      info("this logs fine")
    }

    // include the marker
    logger.withMarker(excludeMarker).info.when(whenCondition) { info =>
      info("this is never logged because state marker sees exclude")
    }

    buffer.take(buffer.size).foreach(println)
  }
}
