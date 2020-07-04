package com.tersesystems.blindsight

import java.time.Instant

import org.slf4j.event.Level

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
