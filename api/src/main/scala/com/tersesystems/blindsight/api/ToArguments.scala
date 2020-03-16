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
 * This is a type class used to convert given types to `Arguments`.
 *
 * `ToArguments` uses a contravariant argument here so that a base
 * class can be used, rather than defining every type.  This has
 * advantages, notably when it comes to `ToArguments[Throwable]`,
 * but does mean that the lowest bound will be applied if in scope.
 *
 * Practically speaking, this means if you defined `ToArguments[Any]`,
 * then everything is going to use that, even if there's a more
 * specific type available.
 *
 * {{{
 * case class Person(name: String, age: Int)
 * implicit val personToArguments: ToArguments[Person] = ToArguments { person =>
 *   import net.logstash.logback.argument.StructuredArguments._
 *   new Arguments(Seq(keyValue("person", person.name)))
 * }
 * }}}
 *
 * @tparam T the type to convert to Arguments
 */
trait ToArguments[-T] {
  def toArguments(instance: => T): Arguments
}

trait LowPriorityToArgumentsImplicits {

  implicit val argumentsToArguments: ToArguments[Arguments] = ToArguments(identity)

  implicit val unitToArguments: ToArguments[Unit] = ToArguments { _ => Arguments.empty }

  implicit val stringToArguments: ToArguments[String] = ToArguments { string =>
    new Arguments(Seq(string))
  }

  implicit val booleanToArguments: ToArguments[Boolean] = ToArguments { bool =>
    new Arguments(Seq(bool))
  }

  implicit val shortToArguments: ToArguments[Short] = ToArguments { short =>
    new Arguments(Seq(short))
  }

  implicit val intToArguments: ToArguments[Int] = ToArguments { int => new Arguments(Seq(int)) }

  implicit val longToArguments: ToArguments[Long] = ToArguments { long => new Arguments(Seq(long)) }

  implicit val floatToArguments: ToArguments[Float] = ToArguments { float =>
    new Arguments(Seq(float))
  }

  implicit val doubleToArguments: ToArguments[Double] = ToArguments { double =>
    new Arguments(Seq(double))
  }

  implicit val throwableToArguments: ToArguments[Throwable] = ToArguments { e =>
    new Arguments(Seq(e))
  }

  implicit def seqArguments: ToArguments[Seq[AsArguments]] = ToArguments { seq =>
    seq.foldLeft(Arguments.empty)((acc, el) => acc + el.arguments)
  }
}

object ToArguments extends LowPriorityToArgumentsImplicits {
  def apply[T: NotNothing](f: T => Arguments): ToArguments[T] = new ToArguments[T] {
    override def toArguments(instance: => T): Arguments = f(instance)
  }
}

class AsArguments(val arguments: Arguments) extends AnyVal
object AsArguments {
  implicit def toAsArguments[A: ToArguments](a: A): AsArguments = {
    val arguments = implicitly[ToArguments[A]].toArguments(a)
    new AsArguments(arguments)
  }
}
