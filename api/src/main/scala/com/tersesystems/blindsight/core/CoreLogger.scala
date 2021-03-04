package com.tersesystems.blindsight.core

import java.time.Instant

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{Condition, Entry, EventBuffer, Markers, ToMarkers}
import org.slf4j.event.Level

/**
 * The core logger.
 *
 * This should not be used directly by end users.
 */
trait CoreLogger
    extends UnderlyingMixin
    with MarkerMixin
    with OnConditionMixin
    with EventBufferMixin
    with EntryTransformMixin {

  type Self = CoreLogger

  def state: CoreLogger.State

  def condition: Condition

  def predicate(level: Level): CorePredicate

  def parameterList(level: Level): ParameterList

  def when(level: Level, condition: Condition): Boolean
}

object CoreLogger {

  private val clock = java.time.Clock.systemUTC()

  /**
   * The state of the core logger.
   */
  trait State {
    def underlying: org.slf4j.Logger

    def markers: Markers

    def condition: Condition

    def parameterLists: Array[ParameterList]

    def withMarker[M: ToMarkers](m: M): State

    def withCondition(c: Condition): State

    def withEntryTransform(level: Level, f: Entry => Entry): State

    def withEntryTransform(f: Entry => Entry): State

    def withParameterLists(lists: Array[ParameterList]): State
  }

  object State {

    final case class Impl(
        markers: Markers,
        underlying: org.slf4j.Logger,
        condition: Condition,
        sourceInfoBehavior: Option[SourceInfoBehavior],
        parameterLists: Array[ParameterList]
    ) extends State {

      def withMarker[M: ToMarkers](m: M): State = {
        val markers = implicitly[ToMarkers[M]].toMarkers(m)
        copy(markers = this.markers + markers)
      }

      def withCondition(c: Condition): State = {
        val f = (level: Level, markers: Markers) => condition(level, markers) && c(level, markers)
        copy(condition = Condition(f))
      }

      def withParameterLists(lists: Array[ParameterList]): State = {
        copy(parameterLists = lists)
      }

      override def withEntryTransform(transformF: Entry => Entry): State = {
        val newLists: Array[ParameterList] = parameterLists.map {
          case proxy: ParameterList.Proxy =>
            new ParameterList.Proxy(proxy.delegate, proxy.transform.andThen(transformF))
          case delegate =>
            new ParameterList.Proxy(delegate, transformF)
        }
        withParameterLists(newLists)
      }

      override def withEntryTransform(level: Level, transformF: Entry => Entry): State = {
        val newLists: Array[ParameterList] = parameterLists.zipWithIndex.map { case (delegate, i) =>
          if (i == level.ordinal()) {
            delegate match {
              case proxy: ParameterList.Proxy =>
                new ParameterList.Proxy(proxy.delegate, proxy.transform.andThen(transformF))
              case delegate =>
                new ParameterList.Proxy(delegate, transformF)
            }
          } else {
            delegate
          }
        }
        withParameterLists(newLists)
      }
    }
  }

  def apply(underlying: org.slf4j.Logger): CoreLogger = {
    apply(underlying, None)
  }

  def apply(
      underlying: org.slf4j.Logger,
      sourceInfoBehavior: Option[SourceInfoBehavior]
  ): CoreLogger = {

    val parameterLists = sourceInfoBehavior match {
      case Some(behavior) =>
        sourceInfo(behavior, lists(underlying))
      case None =>
        lists(underlying)
    }

    val state = State.Impl(
      Markers.empty,
      underlying,
      Condition.always,
      sourceInfoBehavior,
      parameterLists
    )
    new Impl(state)
  }

  /**
   * Common core logger behavior.
   */
  abstract class Abstract extends CoreLogger {
    val state: State

    override def predicate(level: Level): CorePredicate =
      new CorePredicate.Impl(level, this)

    override def markers: Markers = state.markers

    override def underlying: org.slf4j.Logger = state.underlying

    override def condition: Condition = state.condition

    override def parameterList(level: Level): ParameterList = {
      if (state.markers.isEmpty) {
        state.parameterLists(level.ordinal())
      } else {
        new ParameterList.StateMarker(state.markers, state.parameterLists(level.ordinal()))
      }
    }

    override def when(level: Level, condition: Condition): Boolean = {
      // because conditions are AND, if there's a never in the condition we
      // can always return false right off the bat.
      if (condition != Condition.never && condition(level, state.markers)) {
        parameterList(level).executePredicate()
      } else {
        false
      }
    }

    override def withCondition(c: Condition): Self = {
      if (c == Condition.never) {
        new Noop(state)
      } else {
        new Conditional(new Impl(state.withCondition(c)))
      }
    }

    protected def buffered(buffer: EventBuffer, clock: () => Instant): Array[ParameterList] = {
      Array(
        buffered(buffer, Level.ERROR, clock),
        buffered(buffer, Level.WARN, clock),
        buffered(buffer, Level.INFO, clock),
        buffered(buffer, Level.DEBUG, clock),
        buffered(buffer, Level.TRACE, clock)
      )
    }

    protected def buffered(
        buffer: EventBuffer,
        level: Level,
        clock: () => Instant
    ): ParameterList = {
      val loggerName = underlying.getName
      val bufferF = (entry: Entry) => {
        val event = EventBuffer.Event(clock(), loggerName = loggerName, level = level, entry)
        buffer.offer(event)
        entry
      }

      parameterList(level) match {
        case proxy: ParameterList.Proxy =>
          new ParameterList.Proxy(proxy.delegate, proxy.transform.andThen(bufferF))
        case delegate =>
          new ParameterList.Proxy(delegate, bufferF)
      }
    }
  }

  /**
   * Implementation of core logger.
   *
   * @param state the core logger's state.
   */
  class Impl(val state: State) extends Abstract {
    override def withMarker[M: ToMarkers](m: M): CoreLogger = {
      new Impl(state.withMarker(m))
    }

    override def withEntryTransform(level: Level, f: Entry => Entry): Self = {
      new Impl(state.withEntryTransform(level, f))
    }

    override def withEntryTransform(f: Entry => Entry): Self = {
      new Impl(state.withEntryTransform(f))
    }

    override def withEventBuffer(buffer: EventBuffer): Self = {
      val bufferedLists = buffered(buffer, () => clock.instant())
      new Impl(state.withParameterLists(bufferedLists))
    }

    override def withEventBuffer(level: Level, buffer: EventBuffer): Self = {
      val newLists: Array[ParameterList] = new Array(5)
      state.parameterLists.copyToArray(newLists)
      newLists(level.ordinal()) = buffered(buffer, level, () => clock.instant())
      new Impl(state.withParameterLists(newLists))
    }
  }

  /**
   * A core logger running conditionals.
   *
   * @param impl the implementation
   */
  class Conditional(impl: Impl) extends Impl(impl.state) {
    @inline
    override def parameterList(level: Level): ParameterList =
      new ParameterList.Conditional(level, impl)

    override def withMarker[M: ToMarkers](m: M): Self = {
      new Conditional(new Impl(state.withMarker(m)))
    }

    override def withEntryTransform(
        level: Level,
        f: Entry => Entry
    ): CoreLogger = {
      new Conditional(new Impl(state.withEntryTransform(level, f)))
    }

    override def withEntryTransform(f: Entry => Entry): Self = {
      new Conditional(new Impl(state.withEntryTransform(f)))
    }

    override def withEventBuffer(buffer: EventBuffer): Self = {
      val bufferedLists = buffered(buffer, () => clock.instant())
      new Conditional(new Impl(state.withParameterLists(bufferedLists)))
    }

    override def withEventBuffer(level: Level, buffer: EventBuffer): Self = {
      val newLists: Array[ParameterList] = new Array(5)
      state.parameterLists.copyToArray(newLists)
      newLists(level.ordinal()) = buffered(buffer, level, () => clock.instant())
      new Conditional(new Impl(state.withParameterLists(newLists)))
    }
  }

  /**
   * A core logger that does nothing.
   */
  class Noop(val state: State) extends Abstract {
    override def parameterList(level: Level): ParameterList = ParameterList.Noop

    override def when(level: Level, condition: Condition): Boolean = false

    override def withCondition(condition: Condition): Self =
      new Noop(state.withCondition(condition))

    override def withMarker[T: ToMarkers](instance: T): Self =
      new Noop(state.withMarker(instance))

    override def withEntryTransform(f: Entry => Entry): Self = this

    override def withEntryTransform(level: Level, f: Entry => Entry): Self = {
      this // XXX do some testing on this
    }

    override def withEventBuffer(buffer: EventBuffer): Self = {
      val bufferedLists = buffered(buffer, () => clock.instant())
      new Noop(state.withParameterLists(bufferedLists))
    }

    override def withEventBuffer(level: Level, buffer: EventBuffer): Self = {
      val newLists: Array[ParameterList] = new Array(5)
      state.parameterLists.copyToArray(newLists)
      newLists(level.ordinal()) = buffered(buffer, level, () => clock.instant())
      new Noop(state.withParameterLists(newLists))
    }
  }

  /**
   * Indexed by enum ordinal, i.e. to look up, use Level.TRACE.ordinal() as index.
   */
  def lists(logger: org.slf4j.Logger): Array[ParameterList] =
    Array(
      new ParameterList.Error(logger),
      new ParameterList.Warn(logger),
      new ParameterList.Info(logger),
      new ParameterList.Debug(logger),
      new ParameterList.Trace(logger)
    )

  /**
   * Adds source info to the parameter lists.
   */
  private def sourceInfo(
      behavior: SourceInfoBehavior,
      lists: Array[ParameterList]
  ): Array[ParameterList] = {
    Array(
      new ParameterList.WithSource(behavior, lists(Level.ERROR.ordinal())),
      new ParameterList.WithSource(behavior, lists(Level.WARN.ordinal())),
      new ParameterList.WithSource(behavior, lists(Level.INFO.ordinal())),
      new ParameterList.WithSource(behavior, lists(Level.DEBUG.ordinal())),
      new ParameterList.WithSource(behavior, lists(Level.TRACE.ordinal()))
    )
  }

}
