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
