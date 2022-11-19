/*
 * Copyright 2020 com.tersesystems.blindsight
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

import com.tersesystems.blindsight.AST.BObject

import scala.collection.compat.immutable.ArraySeq

/**
 * This class represents an argument to a logging statement.
 *
 * Normally this is used in a [[ToArgument]] type class instance, and you
 * should not have to use it in a logging statement directly.
 *
 * Note that an exception is **not** a valid argument, and exceptions are
 * handled explicitly as `java.lang.Throwable` in the APIs.
 *
 * @param value the argument value.
 */
final class Argument(val value: Any) extends AnyVal {
  def arguments: Arguments   = new Arguments(Array(this))
  def toStatement: Statement = Statement().withArguments(arguments)
}

object Argument {

  /**
   * Converts an instance into an argument.
   */
  def apply[A: ToArgument](instance: A): Argument = implicitly[ToArgument[A]].toArgument(instance)

  def unapply[A: ToArgument](instance: A): Option[Any] = {
    Some(implicitly[ToArgument[A]].toArgument(instance).value)
  }
}

/**
 * This is the representation of arguments in an SLF4J logging statement.  It is used to
 * prevent the use of varadic arguments in the SLF4J API, which is a problem for type safety
 * and also causes problems with `: _*` type ascryption and confused IDEs.
 *
 * Arguments present as an immutable API, which can be composed together using `+` and
 * `++` for sequences.
 *
 * {{{
 * val argsA: Arguments = Arguments("a", 1)
 * val argsPlus: Arguments = argsA + true
 * }}}
 */
final class Arguments(private val elements: Array[Argument]) extends AnyVal {

  def size: Int = elements.length

  def isEmpty: Boolean = elements.isEmpty

  def nonEmpty: Boolean = elements.nonEmpty

  def add[T: ToArgument](instance: T): Arguments = {
    new Arguments(elements :+ Argument(instance))
  }

  def +[T: ToArgument](instance: T): Arguments = add(instance)

  def placeholders: String = " {}" * elements.length

  def toSeq: Seq[Any] = ArraySeq.unsafeWrapArray(toArray)

  def toArray: Array[Any] = elements.map(_.value)
}

object Arguments {
  val empty: Arguments = new Arguments(Array.empty)

  def fromArray(els: Array[Argument]): Arguments = {
    new Arguments(els)
  }

  def fromInstance[A: ToArgument](instance: A): Arguments = {
    val argument = implicitly[ToArgument[A]].toArgument(instance)
    fromArray(Array(argument))
  }

  def apply(varargs: Any*): Arguments = {
    val args: Array[Argument] = varargs.map {
      case arg: Argument => arg
      case bobj: BObject => Argument(bobj)
      case other         => new Argument(other)
    }.toArray
    Arguments.fromArray(args)
  }
}
