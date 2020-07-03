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

    def onCondition(c: Condition): State

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

      def onCondition(c: Condition): State = {
        val f = (level: Level, markers: Markers) => condition(level, markers) && c(level, markers)
        copy(condition = Condition(f))
      }

      def withParameterLists(lists: Array[ParameterList]): State = {
        copy(parameterLists = lists)
      }

      override def withEntryTransform(f: Entry => Entry): State = {
        withParameterLists(transform(parameterLists, f))
      }

      override def withEntryTransform(level: Level, f: Entry => Entry): State = {
        val newParameterLists: Array[ParameterList] = new Array(parameterLists.length)
        parameterLists.copyToArray(newParameterLists)
        val i = level.ordinal()
        newParameterLists(i) = new ParameterList.Proxy(newParameterLists(i), f)
        withParameterLists(newParameterLists)
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

    override def onCondition(c: Condition): CoreLogger = {
      if (c == Condition.never) {
        new Noop(state)
      } else {
        new Conditional(new Impl(state.onCondition(c)))
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

    override def withEntryTransform(level: Level, f: Entry => Entry): CoreLogger = {
      new Impl(state.withEntryTransform(level, f))
    }

    override def withEntryTransform(f: Entry => Entry): CoreLogger = {
      new Impl(state.withEntryTransform(f))
    }

    override def withEventBuffer(buffer: EventBuffer): CoreLogger = {
      val bufferedLists = buffered(this, buffer, () => clock.instant())
      new Impl(state.withParameterLists(bufferedLists))
    }

    override def withEventBuffer(level: Level, buffer: EventBuffer): CoreLogger = {
      ???
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

    override def withMarker[M: ToMarkers](m: M): CoreLogger = {
      new Conditional(new Impl(state.withMarker(m)))
    }

    override def withEntryTransform(
        level: Level,
        f: Entry => Entry
    ): CoreLogger = {
      new Conditional(new Impl(state.withEntryTransform(level, f)))
    }

    override def withEntryTransform(f: Entry => Entry): CoreLogger = {
      new Conditional(new Impl(state.withEntryTransform(f)))
    }

    override def withEventBuffer(buffer: EventBuffer): CoreLogger = {
      val bufferedLists = buffered(this, buffer, () => clock.instant())
      new Conditional(new Impl(state.withParameterLists(bufferedLists)))
    }
  }

  /**
   * A core logger that does nothing.
   */
  class Noop(val state: State) extends Abstract {
    override def parameterList(level: Level): ParameterList = ParameterList.Noop

    override def when(level: Level, condition: Condition): Boolean = false

    override def onCondition(condition: Condition): CoreLogger =
      new Noop(state.onCondition(condition))

    override def withMarker[T: ToMarkers](instance: T): CoreLogger =
      new Noop(state.withMarker(instance))

    override def withEntryTransform(f: Entry => Entry): CoreLogger = this

    override def withEntryTransform(level: Level, f: Entry => Entry): CoreLogger = {
      this // XXX do some testing on this
    }

    override def withEventBuffer(buffer: EventBuffer): CoreLogger = {
      val bufferedLists = buffered(this, buffer, () => clock.instant())
      new Noop(state.withParameterLists(bufferedLists))
    }

    override def withEventBuffer(level: Level, buffer: EventBuffer): CoreLogger = ???
  }

  /**
   * Indexed by enum ordinal, i.e. to look up, use Level.TRACE.ordinal() as index.
   */
  private def lists(logger: org.slf4j.Logger): Array[ParameterList] =
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

  /**
   * Adds an entry transformation step to parameter lists.
   */
  private def transform(
                 lists: Array[ParameterList],
                 transformF: Entry => Entry
               ): Array[ParameterList] = {
    def delegate(level: Level): ParameterList = {
      new ParameterList.Proxy(lists(level.ordinal()), transformF)
    }

    Array(
      delegate(Level.ERROR),
      delegate(Level.WARN),
      delegate(Level.INFO),
      delegate(Level.DEBUG),
      delegate(Level.TRACE)
    )
  }

  /**
   * Adds event buffers and returns parameter lists.
   *
   * @param coreLogger the core logger
   * @param clock an instant producing function
   * @return array of lists that offer entry to the buffer.
   */
  private def buffered(
                coreLogger: CoreLogger,
                buffer: EventBuffer,
                clock: () => Instant
              ): Array[ParameterList] = {
    Array(
      buffered(coreLogger, buffer, Level.ERROR, clock),
      buffered(coreLogger, buffer, Level.WARN, clock),
      buffered(coreLogger, buffer, Level.INFO, clock),
      buffered(coreLogger, buffer, Level.DEBUG, clock),
      buffered(coreLogger, buffer, Level.TRACE, clock)
    )
  }

  /**
   * Adds an event offering function to a single parameter list.
   *
   * @param coreLogger the core logger
   * @param level the level to log at
   * @param clock an instant producing function
   * @return a single parameter list
   */
  private def buffered(
                coreLogger: CoreLogger,
                buffer: EventBuffer,
                level: Level,
                clock: () => Instant
              ): ParameterList = {
    val loggerName = coreLogger.state.underlying.getName
    val bufferF = (entry: Entry) => {
      val event = EventBuffer.Event(clock(), loggerName = loggerName, level = level, entry)
      buffer.offer(event)
      entry
    }
    new ParameterList.Proxy(coreLogger.parameterList(level), bufferF)
  }

}
