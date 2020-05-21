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

package com.tersesystems.blindsight

import org.slf4j.{Marker, MarkerFactory}

/**
 * This class represents the markers that are associated with the logging statement.
 *
 * Markers represents all added `org.slf4j.Marker` instances together in an immutable Set,
 * and only at the end wrapped into a marker with `markers.marker`.  This generated marker
 * uses the internal hashcode and contains all the markers in the set as direct children.
 *
 * {{{
 * val markers: Markers = Markers(marker)
 * }}}
 *
 * Markers does not directly use the SLF4J marker API, as directly mutating markers
 * together as "child references" so that only one marker is available is generally unsafe.
 */
final class Markers(private val internal: Set[Marker]) {

  lazy val marker: Marker = {
    val init = MarkerFactory.getDetachedMarker(Integer.toHexString(internal.hashCode))
    internal.foldLeft(init) { (acc, el) => acc.add(el); acc; }
  }

  def isEmpty: Boolean = internal.isEmpty

  def nonEmpty: Boolean = internal.nonEmpty

  def size: Int = internal.size

  def contains(elem: Marker): Boolean = internal.contains(elem)

  def iterator: Iterator[Marker] = internal.iterator

  def +[T: ToMarkers](elem: T): Markers = {
    val markers = implicitly[ToMarkers[T]].toMarkers(elem)
    new Markers(internal ++ markers.internal)
  }

  def -[T: ToMarkers](elem: T): Markers = {
    val markers = implicitly[ToMarkers[T]].toMarkers(elem)
    new Markers(internal -- markers.internal)
  }

  def toStatement: Statement = Statement().withMarkers(this)
}

object Markers {
  def empty: Markers = new Markers(Set.empty)

  def apply[T: ToMarkers](instance: => T): Markers = implicitly[ToMarkers[T]].toMarkers(instance)

  // termination point for markerToMarkers so it's not recursive
  def apply(element: Marker): Markers = new Markers(Set(element))
}
