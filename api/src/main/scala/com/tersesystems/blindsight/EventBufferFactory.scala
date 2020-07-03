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
    val iter = bufferFactoryLoader.iterator()
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
