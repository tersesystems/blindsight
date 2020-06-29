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
 * This is a type class used to convert given types to `Message`.
 *
 * {{{
 * case class Person(name: String, age: Int)
 * implicit val personToMessage: ToMessage[Foo] = ToMessage { person =>
 *   new Message(person.name)
 * }
 * }}}
 *
 * @tparam T the type to convert to Message
 */
trait ToMessage[T] {
  def toMessage(instance: T): Message
}

trait LowPriorityToMessageImplicits {

  implicit val messageToMessage: ToMessage[Message] = ToMessage(identity)

  implicit val stringToMessage: ToMessage[String] = ToMessage { str => new Message(str) }

  implicit val tupleToMessage: ToMessage[(String, String)] = ToMessage {
    case (k, v) =>
      Message(s"$k=$v")
  }
}

object ToMessage extends LowPriorityToMessageImplicits {
  def apply[T: NotNothing](f: T => Message): ToMessage[T] =
    new ToMessage[T] {
      override def toMessage(instance: T): Message = f(instance)
    }
}
