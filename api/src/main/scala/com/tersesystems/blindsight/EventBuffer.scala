/*
 * Copyright 2020 com.tersesystems.blindsight
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

import org.slf4j.event.Level

import java.time.Instant

/**
 * An event buffer contains events that were about to be logged into SLF4J.
 *
 * Event buffers are very useful for testing and for keeping diagnostic log entries
 * for a brief period of time, but may not have 1:1 equivalence with what ends up in
 * the logs.
 */
trait EventBuffer {

  def offer(event: EventBuffer.Event): Unit

  def size: Int

  def capacity: Int

  def isEmpty: Boolean

  def clear(): Unit

  def take(count: Int): scala.collection.immutable.Seq[EventBuffer.Event]

  def headOption: Option[EventBuffer.Event]

  def head: EventBuffer.Event
}

object EventBuffer {

  /**
   * An event is an entry that was added to a buffer.
   *
   * @param timestamp the instant the entry was added to the buffer
   * @param loggerName the SLF4J logger name used to add to the buffer
   * @param level the level added to buffer
   * @param entry the entry itself.
   */
  final case class Event(
      timestamp: Instant,
      loggerName: String,
      level: Level,
      entry: Entry
  )

  def apply(capacity: Int): EventBuffer = EventBufferFactory().create(capacity)
}
