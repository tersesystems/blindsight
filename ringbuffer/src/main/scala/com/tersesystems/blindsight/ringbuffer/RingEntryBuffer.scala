package com.tersesystems.blindsight.ringbuffer

import com.tersesystems.blindsight.{Entry, EntryBuffer}

import scala.collection.JavaConverters._

class RingEntryBuffer(initCapacity: Int) extends EntryBuffer {
  import org.jctools.queues.MpmcArrayQueue

  private val queue: MpmcArrayQueue[Entry] = new MpmcArrayQueue(initCapacity)

  override def offer(entry: Entry): Unit = queue.relaxedOffer(entry)

  override def size: Int = queue.size()

  override def take(count: Int): Seq[Entry] = queue.iterator().asScala.take(count).toSeq

  override def headOption: Option[Entry] = Option(queue.relaxedPeek())

  override def head: Entry = queue.relaxedPeek()

  override def isEmpty: Boolean = queue.isEmpty

  override def clear(): Unit = queue.clear()

  override def capacity: Int = queue.capacity()
}
