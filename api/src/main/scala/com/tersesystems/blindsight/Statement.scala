/*
 * Copyright 2020 Terse Systems
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

/**
 * A statement represents a total logging statement.  This is most useful
 * for semantic logging, which converts objects to [[Statement]], but is
 * generally helpful in packaging as well.
 */
trait Statement {
  def markers: Markers
  def message: Message
  def arguments: Arguments
  def throwable: Option[Throwable]

  def withArguments(args: Arguments): Statement
  def withMarkers[T: ToMarkers](markers: T): Statement
  def withMessage[T: ToMessage](message: T): Statement
  def withThrowable(t: Throwable): Statement
}

object Statement {

  final case class Impl private (
      markers: Markers = Markers.empty,
      message: Message = Message.empty,
      arguments: Arguments = Arguments.empty,
      throwable: Option[Throwable] = None
  ) extends Statement {

    def withArguments(args: Arguments): Statement = {
      copy(arguments = args)
    }

    def withMarkers[T: ToMarkers](markers: T): Statement = {
      copy(markers = implicitly[ToMarkers[T]].toMarkers(markers))
    }

    def withMessage[T: ToMessage](message: T): Statement = {
      copy(message = implicitly[ToMessage[T]].toMessage(message))
    }

    def withThrowable(t: Throwable): Statement = {
      copy(throwable = Some(t))
    }
  }

  def unapply(st: Statement): Option[(Markers, Message, Arguments, Option[Throwable])] = {
    Some((st.markers, st.message, st.arguments, st.throwable))
  }

  def apply(): Statement = Impl()

  def apply(message: Message): Statement = Impl(message = message)

  def apply(markers: Markers, message: Message): Statement =
    Impl(markers = markers, message = message)

  def apply(message: Message, arguments: Arguments): Statement =
    Impl(message = message, arguments = arguments)

  def apply(message: Message, throwable: Throwable): Statement =
    Impl(message = message, throwable = Some(throwable))

  def apply(message: Message, arguments: Arguments, throwable: Throwable): Statement =
    Impl(message = message, arguments = arguments, throwable = Some(throwable))

  def apply(markers: Markers, message: Message, arguments: Arguments): Statement =
    Impl(markers = markers, message = message, arguments = arguments)

  def apply(markers: Markers, message: Message, throwable: Throwable): Statement =
    Impl(markers, message = message, throwable = Some(throwable))

  def apply(markers: Markers, message: Message, arguments: Arguments, t: Throwable): Statement =
    Impl(markers = markers, message = message, arguments = arguments, throwable = Some(t))

}
