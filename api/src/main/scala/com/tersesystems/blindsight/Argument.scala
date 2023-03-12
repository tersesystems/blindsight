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

import scala.collection.compat.immutable.ArraySeq

/**
 * This class represents an argument to a logging statement.
 *
 * Normally this is used in a [[ToArgument]] type class instance, and you
 * should not have to use it in a logging statement directly.
 *
 * @param value the argument value.
 */
final class Argument(val value: Any) extends AnyVal {
  def arguments: Arguments   = Arguments(new Argument(value))
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
 * This is the representation of arguments in an SLF4J logging statement. 
 *
 * You shouldn't need to use this most of the time, as its used for when there are more arguments
 * than the API can handle directly.
 *
 * Arguments present as an immutable API, which can be composed together using `+` and
 * `++` for sequences.
 *
 * {{{
 * val argsA: Arguments = Arguments("a", 1)
 * val argsPlus: Arguments = argsA + true
 * }}}
 */
final class Arguments private (private val elements: Array[Argument]) extends AnyVal {

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

  def fromSeq(els: Seq[Argument]): Arguments = {
    new Arguments(els.toArray)
  }

  def fromInstance[A: ToArgument](instance: A): Arguments = apply(instance)

  def apply[A1: ToArgument](a1: A1): Arguments = {
    fromArray(Array(Argument(a1)))
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument
  ](a1: A1, a2: A2): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument
  ](a1: A1, a2: A2, a3: A3): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4, a5: A5): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8)
      )
    )
  }

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8, a9: A9): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9)
      )
    )
  }

  // 10
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument
  ](a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8, a9: A9, a10: A10): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10)
      )
    )
  }

  // 15
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11)
      )
    )
  }

  // 12
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12)
      )
    )
  }

  // 13
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13)
      )
    )
  }

  // 14
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14)
      )
    )
  }

  // 15
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15)
      )
    )
  }

  // 16
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16)
      )
    )
  }

  // 17
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16,
      a17: A17
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16),
        Argument(a17)
      )
    )
  }

  // 18
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16,
      a17: A17,
      a18: A18
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16),
        Argument(a17),
        Argument(a18)
      )
    )
  }

  // 19
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16,
      a17: A17,
      a18: A18,
      a19: A19
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16),
        Argument(a17),
        Argument(a18),
        Argument(a19)
      )
    )
  }

  // 20
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument,
      A20: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16,
      a17: A17,
      a18: A18,
      a19: A19,
      a20: A20
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16),
        Argument(a17),
        Argument(a18),
        Argument(a19),
        Argument(a20)
      )
    )
  }

  // 21
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument,
      A20: ToArgument,
      A21: ToArgument,
      A22: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16,
      a17: A17,
      a18: A18,
      a19: A19,
      a20: A20,
      a21: A21
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16),
        Argument(a17),
        Argument(a18),
        Argument(a19),
        Argument(a20),
        Argument(a21)
      )
    )
  }

  // 22
  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument,
      A20: ToArgument,
      A21: ToArgument,
      A22: ToArgument
  ](
      a1: A1,
      a2: A2,
      a3: A3,
      a4: A4,
      a5: A5,
      a6: A6,
      a7: A7,
      a8: A8,
      a9: A9,
      a10: A10,
      a11: A11,
      a12: A12,
      a13: A13,
      a14: A14,
      a15: A15,
      a16: A16,
      a17: A17,
      a18: A18,
      a19: A19,
      a20: A20,
      a21: A21,
      a22: A22
  ): Arguments = {
    fromArray(
      Array(
        Argument(a1),
        Argument(a2),
        Argument(a3),
        Argument(a4),
        Argument(a5),
        Argument(a6),
        Argument(a7),
        Argument(a8),
        Argument(a9),
        Argument(a10),
        Argument(a11),
        Argument(a12),
        Argument(a13),
        Argument(a14),
        Argument(a15),
        Argument(a16),
        Argument(a17),
        Argument(a18),
        Argument(a19),
        Argument(a20),
        Argument(a21),
        Argument(a22)
      )
    )
  }
}
