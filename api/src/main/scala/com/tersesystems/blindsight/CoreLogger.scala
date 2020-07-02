package com.tersesystems.blindsight

import com.tersesystems.blindsight.mixins._
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
    with EntryBufferMixin
    with EntryTransformMixin {

  type Self = CoreLogger

  def state: CoreLogger.State

  def condition: Condition

  def predicate(level: Level): SimplePredicate

  def parameterList(level: Level): ParameterList

  def when(level: Level, condition: Condition): Boolean
}

object CoreLogger {

  /**
   * The state of the core logger.
   */
  trait State {
    def underlying: org.slf4j.Logger

    def markers: Markers

    def condition: Condition

    def parameterLists: Array[ParameterList]

    def entries: Option[EntryBuffer]

    def withMarker[M: ToMarkers](m: M): State

    def onCondition(c: Condition): State

    def withEntryBuffer(buffer: EntryBuffer): State

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
        parameterLists: Array[ParameterList],
        entries: Option[EntryBuffer]
    ) extends State {

      def withMarker[M: ToMarkers](m: M): State = {
        val markers = implicitly[ToMarkers[M]].toMarkers(m)
        copy(markers = this.markers + markers)
      }

      def onCondition(c: Condition): State = {
        val f = (level: Level, s: State) => condition(level, s) && c(level, s)
        copy(condition = Condition(f))
      }

      def withParameterLists(lists: Array[ParameterList]): State = {
        copy(parameterLists = lists)
      }

      override def withEntryBuffer(buffer: EntryBuffer): State = {
        val bufferedLists = ParameterList.buffered(parameterLists, buffer)
        copy(entries = Option(buffer), parameterLists = bufferedLists)
      }

      override def withEntryTransform(f: Entry => Entry): State = {
        withParameterLists(ParameterList.transform(parameterLists, f))
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
    val state = State.Impl(
      Markers.empty,
      underlying,
      Condition.always,
      sourceInfoBehavior,
      ParameterList.lists(underlying),
      None
    )
    new Impl(state)
  }

  /**
   * Common core logger behavior.
   */
  abstract class Abstract extends CoreLogger {
    val state: State

    override def predicate(level: Level): SimplePredicate =
      new SimplePredicate.Impl(level, this)

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

    override def entries: Option[EntryBuffer] = state.entries

    override def when(level: Level, condition: Condition): Boolean = {
      // because conditions are AND, if there's a never in the condition we
      // can always return false right off the bat.
      if (condition != Condition.never && condition(level, state)) {
        val list = parameterList(level)
        if (state.markers.isEmpty) {
          list.executePredicate()
        } else {
          list.executePredicate(state.markers.marker)
        }
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

    override def withEntryBuffer(buffer: EntryBuffer): CoreLogger = {
      new Impl(state.withEntryBuffer(buffer))
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

    override def withEntryBuffer(buffer: EntryBuffer): CoreLogger =
      new Conditional(new Impl(state.withEntryBuffer(buffer)))
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

    override def withEntryBuffer(buffer: EntryBuffer): CoreLogger =
      new Noop(state.withEntryBuffer(buffer))

  }

}
