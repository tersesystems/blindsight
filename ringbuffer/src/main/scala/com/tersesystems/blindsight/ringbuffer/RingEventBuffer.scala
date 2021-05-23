package com.tersesystems.blindsight.ringbuffer

import com.tersesystems.blindsight.EventBuffer

import java.util.stream.Collectors

/**
 * Ring buffer backed by <a href="https://github.com/JCTools/JCTools">MpmcArrayQueue</a>.
 *
 * @param initCapacity the initial capacity of the ring buffer.
 */
class RingEventBuffer(initCapacity: Int) extends EventBuffer {

  import org.jctools.queues.MpmcArrayQueue

  private val queue: MpmcArrayQueue[EventBuffer.Event] = new MpmcArrayQueue(initCapacity)

  override def offer(event: EventBuffer.Event): Unit = {
    queue.relaxedOffer(event)
  }

  override def size: Int = queue.size()

  override def take(count: Int): scala.collection.immutable.Seq[EventBuffer.Event] = {
    import scala.collection.compat.immutable._
    val events = queue.stream().limit(count).collect(Collectors.toList[EventBuffer.Event]())
    val newArray: Array[EventBuffer.Event] = new Array(count)
    events.toArray(newArray)
    ArraySeq.unsafeWrapArray(newArray)
  }

  override def headOption: Option[EventBuffer.Event] = Option(queue.relaxedPeek())

  override def head: EventBuffer.Event = queue.relaxedPeek()

  override def isEmpty: Boolean = queue.isEmpty

  override def clear(): Unit = queue.clear()

  override def capacity: Int = queue.capacity()
}
