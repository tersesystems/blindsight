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

trait EventBufferFactory {

  /**
   * Create event buffer with a fixed capacity.
   *
   * @param capacity the maximum entries in the entry buffer.
   * @return a new entry buffer.
   */
  def create(capacity: Int): EventBuffer
}

object EventBufferFactory {

  import java.util.ServiceLoader

  private lazy val bufferFactory: EventBufferFactory = {
    // We don't need a buffer factory loaded, so don't throw
    // until we're asked for one...
    val bufferFactoryLoader = ServiceLoader.load(classOf[EventBufferFactory])

    var factory: EventBufferFactory = null;
    val iter                        = bufferFactoryLoader.iterator()
    while (iter.hasNext && factory == null) {
      factory = iter.next()
    }
    if (factory == null) {
      throw new javax.management.ServiceNotFoundException("No event buffer factory found!")
    } else {
      factory
    }
  }

  def apply(): EventBufferFactory = bufferFactory
}
