package com.tersesystems.blindsight.ringbuffer

import com.tersesystems.blindsight.EventBuffer.Event
import com.tersesystems.blindsight.EventBuffer

/**
 * Ring buffer backed by <a href="https://github.com/JCTools/JCTools">MpmcArrayQueue</a>.
 *
 * @param initCapacity the initial capacity of the ring buffer.
 * @param clock the clock to use to create instants.
 */
class RingEventBuffer(initCapacity: Int) extends EventBuffer {

  import org.jctools.queues.MpmcArrayQueue

  private val queue: MpmcArrayQueue[EventBuffer.Event] = new MpmcArrayQueue(initCapacity)

  override def offer(event: EventBuffer.Event): Unit = {
    queue.relaxedOffer(event)
  }

  override def size: Int = queue.size()

  override def take(count: Int): scala.collection.immutable.Seq[EventBuffer.Event] = {
    import scala.collection.JavaConverters._
    queue.iterator.asScala.take(count).toIndexedSeq
  }

  override def headOption: Option[EventBuffer.Event] = Option(queue.relaxedPeek())

  override def head: EventBuffer.Event = queue.relaxedPeek()

  override def isEmpty: Boolean = queue.isEmpty

  override def clear(): Unit = queue.clear()

  override def capacity: Int = queue.capacity()
}
