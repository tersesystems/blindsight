package com.tersesystems.blindsight.ringbuffer

import java.time.Clock

import com.tersesystems.blindsight.EntryBuffer.El
import com.tersesystems.blindsight._

/**
 * Ring buffer backed by <a href="https://github.com/JCTools/JCTools">MpmcArrayQueue</a>.
 *
 * @param initCapacity the initial capacity of the ring buffer.
 * @param clock the clock to use to create instants.
 */
class RingEntryBuffer(initCapacity: Int, clock: Clock)
  extends EntryBuffer {

  import org.jctools.queues.MpmcArrayQueue

  private val queue: MpmcArrayQueue[El] = new MpmcArrayQueue(initCapacity)

  override def offer(entry: Entry): Unit = {
    queue.relaxedOffer(El(clock.instant(), entry))
  }

  override def size: Int = queue.size()

  override def take(count: Int): scala.collection.immutable.Seq[EntryBuffer.El] = {
    import scala.collection.JavaConverters._
    queue.iterator.asScala.take(count).toIndexedSeq
  }

  override def headOption: Option[EntryBuffer.El] = Option(queue.relaxedPeek())

  override def head: EntryBuffer.El = queue.relaxedPeek()

  override def isEmpty: Boolean = queue.isEmpty

  override def clear(): Unit = queue.clear()

  override def capacity: Int = queue.capacity()
}
