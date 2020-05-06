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

package com.tersesystems.blindsight.api

/**
 * This class represents the message portion of a logging statement.
 *
 * A `Message` is immutable, and so composing a message is a concatenation
 * of `Message + Message`.
 *
 * There is an implicit conversion of string to message, but for general purpose,
 * `ToMessage` should be used to convert objects.
 *
 * {{{
 * val message = Message("foo")
 * logger.info(message)
 * }}}
 */
final class Message(private val raw: String) extends AnyVal {

  def +(message: Message): Message = new Message(raw + message.raw)

  def isEmpty: Boolean = raw.isEmpty

  def nonEmpty: Boolean = raw.nonEmpty

  override def toString: String = raw

  def withPlaceHolders(args: Arguments): Message = {
    new Message(raw + args.placeholders)
  }

  def toStatement: Statement = Statement().withMessage(this)
}

object Message {
  import scala.language.implicitConversions

  implicit def stringToMessage(str: String): Message = new Message(str)

  def empty: Message = new Message("")

  def apply[T: ToMessage](instance: => T): Message = implicitly[ToMessage[T]].toMessage(instance)
}
