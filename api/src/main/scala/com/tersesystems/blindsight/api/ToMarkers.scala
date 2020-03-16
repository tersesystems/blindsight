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

package com.tersesystems.blindsight.api

import org.slf4j.Marker

/**
 * This is a type class used to convert given types to `Markers`.
 *
 * Markers are very useful for structured logging, because they can be
 * converted into key/value pairs without altering the structure of a
 * message or keeping the ordering in arguments (especially since an
 * exception must be at the end of the tail).
 *
 * The API does not define type class instances in this layer, but
 * the logstash package provides many useful mappings to Markers.
 *
 * {{{
 * case class Person(name: String, age: Int)
 * implicit val personToMarkers: ToMarkers[Person] = ToMarkers { person =>
 *   import net.logstash.logback.marker.{Markers => LogstashMarkers}
 *   val personMarker = LogstashMarkers.append("name", person.name)
 *   Markers(personMarker)
 * }
 * }}}
 *
 * @tparam T the type to convert to Markers
 */
trait ToMarkers[T] {
  def toMarkers(instance: T): Markers
}

trait LowPriorityToMarkersImplicits {
  implicit val markersToMarkers: ToMarkers[Markers] = ToMarkers(identity)
  implicit val markerToMarkers: ToMarkers[Marker]   = ToMarkers(Markers(_))
}

object ToMarkers extends LowPriorityToMarkersImplicits {
  def apply[T: NotNothing](f: T => Markers): ToMarkers[T] = new ToMarkers[T] {
    override def toMarkers(instance: T): Markers = f(instance)
  }
}
