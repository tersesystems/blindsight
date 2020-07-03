package com.tersesystems.blindsight

import java.time.Instant

/**
 * A client view of logging entries that were made recently.
 */
trait EntryBuffer {

  def offer(entry: Entry): Unit

  def size: Int

  def capacity: Int

  def isEmpty: Boolean

  def clear(): Unit

  def take(count: Int): scala.collection.immutable.Seq[EntryBuffer.El]

  def headOption: Option[EntryBuffer.El]

  def head: EntryBuffer.El
}

object EntryBuffer {

  final case class El(
    bufferedAt: Instant,
    entry: Entry
  )

  def apply(capacity: Int): EntryBuffer = EntryBufferFactory().create(capacity)
}