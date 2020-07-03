package com.tersesystems.blindsight

/**
 * A client view of logging entries that were made recently.
 */
trait EntryBuffer {

  def offer(entry: Entry): Unit

  def size: Int

  def capacity: Int

  def isEmpty: Boolean

  def clear(): Unit

  def take(count: Int): scala.collection.immutable.Seq[Entry]

  def headOption: Option[Entry]

  def head: Entry
}

trait EntryBufferFactory {
  def createEntryBuffer(capacity: Int): EntryBuffer
}

// XXX should be EntryBufferFactory
object EntryBuffer {

  import java.util.ServiceLoader

  private lazy val bufferFactory: EntryBufferFactory = {
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
  private val bufferFactoryLoader = ServiceLoader.load(classOf[EntryBufferFactory])

  def apply(capacity: Int): EntryBuffer = bufferFactory.createEntryBuffer(capacity)
}
