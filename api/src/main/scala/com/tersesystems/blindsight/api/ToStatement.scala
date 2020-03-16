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
 * Type class for mapping to a `Statement`.
 *
 * {{{
 * case class Person(name: String, age: Int)
 * implicit val personToStatement: ToStatement[Person] = ToStatement { person =>
 *   Statement(markers = Markers.empty,
 *             message = Message("Person({}, {})"),
 *             arguments = Arguments(person.name) + person.age,
 *             throwable = None)
 * }
 * }}}
 *
 * @tparam T the type to convert to a `Statement`
 */
trait ToStatement[T] {
  def toStatement(instance: => T): Statement
}

trait LowPriorityToStatementImplicits {
  implicit val statementToStatement: ToStatement[Statement] = ToStatement(identity)

  implicit val markersToStatement: ToStatement[Markers] =
    ToStatement(instance => instance.toStatement)
  implicit val argumentsToStatement: ToStatement[Arguments] =
    ToStatement(instance => instance.toStatement)
  implicit val messageToStatement: ToStatement[Message] =
    ToStatement(instance => instance.toStatement)
}

object ToStatement extends LowPriorityToStatementImplicits {
  def apply[T, S <: Statement: NotNothing](f: T => S): ToStatement[T] = new ToStatement[T] {
    override def toStatement(instance: => T): Statement = f(instance)
  }
}
