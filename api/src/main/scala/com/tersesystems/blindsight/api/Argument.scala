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
final class Argument(private val el: Any) extends AnyVal {

  //  def add[T: ToArgument](instance: T): Argument = {
  //    val args = implicitly[ToArgument[T]].toArgument(instance)
  //    new Argument(elements ++ args.elements)
  //  }
  //
  //  def +[T: ToArgument](instance: T): Argument = add(instance)
  //
  //  def append(asArguments: Seq[AsArguments]): Argument = {
  //    val args = Argument(asArguments: _*)
  //    new Argument(elements ++ args.elements)
  //  }
  //
  //  def ++(asArguments: Seq[AsArguments]): Argument = append(asArguments)

  def head: Any = el

  def arguments: Arguments = new Arguments(el)

  def toStatement: Statement = Statement().withArguments(this)
}

object Argument {
  def apply[A: ToArgument](instance: A): Argument = implicitly[ToArgument[A]].toArgument(instance)
}

final class Arguments(private val args: Seq[Argument]) {
  def toSeq: Seq[Any] = args.map(_.head)

  def toArray: Array[Any] = toSeq.toArray
}

object Arguments {
  def empty: Arguments = new Arguments(Seq.empty)

  def apply[A: ToArgument](input: A): Arguments = {
    new Arguments(Seq(ToArgument(input)))}

  def apply(input: Iterable[Argument]): Arguments = {
    new Arguments(input.toSeq)
  }
  //
  //  def apply(els: AsArguments*): Seq[Argument] = {
  //    els.foldLeft(Argument.empty)((acc, el) => acc ++ el.arguments)
  //  }
  //
  //  def apply(iterable: Iterable[AsArguments]): Seq[Argument] = {
  //    iterable.iterator.foldLeft(Argument.empty)((acc, el) => acc ++ el.arguments)
  //  }
}

