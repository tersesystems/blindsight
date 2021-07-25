package com.tersesystems.blindsight.fixtures

import ch.qos.logback.core.Context
import ch.qos.logback.core.spi.ContextAware
import org.slf4j.Marker
import java.util
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport

object StreamUtils {

  /**
     * Returns a stream containing the marker itself and the iterator belonging to the marker.
     *
     * @param marker the marker
     * @return empty stream if marker is null, stream.of(marker) if no references, otherwise
     * Stream.of(marker, references)
     */
  def fromMarker(marker: Marker): Stream[Marker] = {
    if (marker == null) return Stream.empty
    if (!marker.hasReferences) return Stream.of(marker)
    Stream.concat(Stream.of(marker), fromIterator(marker.iterator))
  }

  def fromMarker(context: Context, marker: Marker): Stream[Marker] =
    fromMarker(marker: Marker).peek((s: Marker) => setContext(context, s))

  def fromIterator[E](iterator: util.Iterator[E]): Stream[E] = {
    val spliterator = Spliterators.spliteratorUnknownSize(iterator, 0)
    StreamSupport.stream(spliterator, false)
  }

  private def setContext[E](context: Context, s: E): Unit =
    if (s.isInstanceOf[ContextAware]) s.asInstanceOf[ContextAware].setContext(context)
}
