package com.tersesystems.blindsight

trait EntryBufferFactory {

  /**
   * Create entry buffer with a fixed capacity.
   *
   * @param capacity the maximum entries in the entry buffer.
   * @return a new entry buffer.
   */
  def create(capacity: Int): EntryBuffer
}

object EntryBufferFactory {

  import java.util.ServiceLoader

  private lazy val bufferFactory: EntryBufferFactory = {
    // We don't need a buffer factory loaded, so don't throw
    // until we're asked for one...
    val bufferFactoryLoader = ServiceLoader.load(classOf[EntryBufferFactory])

    var factory: EntryBufferFactory = null;
    val iter = bufferFactoryLoader.iterator()
    while (iter.hasNext && factory == null) {
      factory = iter.next()
    }
    if (factory == null) {
      throw new javax.management.ServiceNotFoundException("No buffer factory found!")
    } else {
      factory
    }
  }

  def apply(): EntryBufferFactory = bufferFactory
}
