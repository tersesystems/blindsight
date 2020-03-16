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
 * This is the representation of arguments in an SLF4J logging statement.
 *
 * Arguments present as an immutable API, which can be composed together using `+` and
 * `++` for sequences.
 *
 * {{{
 * val argsA: Arguments = Arguments("a")
 * val argsPlus: Arguments = argsA + "b"
 * val argsPlusPlus: Arguments = argsPlus ++ Seq(1, "c")
 * val messageWithPlaceHolders = Message("some message").withPlaceHolders(argsPlusPlus)
 * logger.info(messageWithPlaceHolders, argsPlusPlus)
 * }}}
 *
 * There is no special treatment of exceptions; as in SLF4J, the exception must be the
 * last element of arguments to be treated as the Throwable.
 */
final class Arguments(private val elements: Seq[Any]) {

  def placeholders: String = " {}" * elements.size

  def asArray: Array[Any] = elements.toArray

  def size: Int = elements.size

  def nonEmpty: Boolean = elements.nonEmpty

  def isEmpty: Boolean = elements.isEmpty

  def add[T: ToArguments](instance: T): Arguments = {
    val args = implicitly[ToArguments[T]].toArguments(instance)
    new Arguments(elements ++ args.elements)
  }

  def +[T: ToArguments](instance: T): Arguments = add(instance)

  def append(asArguments: Seq[AsArguments]): Arguments = {
    val args = Arguments(asArguments: _*)
    new Arguments(elements ++ args.elements)
  }

  def ++(asArguments: Seq[AsArguments]): Arguments = append(asArguments)

  def toStatement: Statement = Statement().withArguments(this)
}

object Arguments {
  def empty: Arguments = new Arguments(Seq.empty)

  def apply(els: AsArguments*): Arguments = {
    els.foldLeft(Arguments.empty)((acc, el) => acc + el.arguments)
  }

  def apply(iterable: Iterable[AsArguments]): Arguments = {
    iterable.iterator.foldLeft(Arguments.empty)((acc, el) => acc + el.arguments)
  }
}
