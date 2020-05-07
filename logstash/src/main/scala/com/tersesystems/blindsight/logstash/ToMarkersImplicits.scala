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

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api.{AsMarkers, Markers, ToArgument, ToMarkers}
import net.logstash.logback.argument.StructuredArguments
import net.logstash.logback.marker.Markers._
import net.logstash.logback.marker.LogstashMarker
import sourcecode.{Args, Enclosing, File, Line}

import scala.collection.JavaConverters._

/**
 * This trait contains type class instances for tuples and lists that convert to logstash markers.
 */
trait LogstashToMarkersImplicits {
  implicit val logstashMarkerToMarkers: ToMarkers[LogstashMarker] = ToMarkers(Markers(_))

  implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
    case (k, v) => Markers(append(k, v))
  }

  implicit val tupleBooleanToMarkers: ToMarkers[(String, Boolean)] = ToMarkers {
    case (k, v) => Markers(append(k, v))
  }

  implicit def tupleNumericToMarkers[T: Numeric]: ToMarkers[(String, T)] = ToMarkers {
    case (k, v) => Markers(append(k, v))
  }

}

/**
 * This trait contains type class instances that convert source code structures into logstash markers.
 */
trait SourceCodeToMarkersImplicits {

  implicit val fileToMarkers: ToMarkers[File] = ToMarkers { file =>
    Markers(append("source.file", file.value))
  }

  implicit val lineToMarkers: ToMarkers[Line] = ToMarkers { line =>
    Markers(append("source.line", line.value))
  }

  implicit val enclosingToMarkers: ToMarkers[Enclosing] = ToMarkers { enclosing =>
    Markers(append("source.enclosing", enclosing.value))
  }

  implicit val argsToMarkers: ToMarkers[Args] = ToMarkers[Args] { sourceArgs =>
    Markers {
      val args: Map[String, Any] =
        sourceArgs.value.flatMap(_.map(a => a.source -> a.value)).toMap
      append("source.arguments", StructuredArguments.entries(args.asJava))
    }
  }

}

trait ToMarkersImplicits extends LogstashToMarkersImplicits with SourceCodeToMarkersImplicits

object ToMarkersImplicits extends ToMarkersImplicits
