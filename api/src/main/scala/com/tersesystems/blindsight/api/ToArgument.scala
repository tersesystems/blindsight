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
 * This is a type class used to convert given types to `Argument`.
 *
 * `ToArguments` uses a contravariant argument here so that a base
 * class can be used, rather than defining every type.  This has
 * advantages, notably when it comes to `ToArgument[Throwable]`,
 * but does mean that the lowest bound will be applied if in scope.
 *
 * Practically speaking, this means if you defined `ToArgument[Any]`,
 * then everything is going to use that, even if there's a more
 * specific type available.
 *
 * {{{
 * case class Person(name: String, age: Int)
 * implicit val personToArgument: ToArgument[Person] = ToArgument { person =>
 *   import net.logstash.logback.argument.StructuredArguments._
 *   new Argument(keyValue("person", person.name))
 * }
 * }}}
 *
 * @tparam T the type to convert to Arguments
 */
trait ToArgument[-T] {
  def toArgument(instance: => T): Argument
}

trait LowPriorityToArgumentImplicits {

  implicit val argumentToArgument: ToArgument[Argument] = ToArgument(identity)

  implicit val unitToArguments: ToArgument[Unit] = ToArgument { _ => Argument.empty }

  implicit val stringToArgument: ToArgument[String] = ToArgument { string =>
    new Argument(Seq(string))
  }

  implicit val booleanToArgument: ToArgument[Boolean] = ToArgument { bool =>
    new Argument(Seq(bool))
  }

  implicit val shortToArgument: ToArgument[Short] = ToArgument { short =>
    new Argument(Seq(short))
  }

  implicit val intToArgument: ToArgument[Int] = ToArgument { int => new Argument(Seq(int)) }

  implicit val longToArgument: ToArgument[Long] = ToArgument { long => new Argument(Seq(long)) }

  implicit val floatToArgument: ToArgument[Float] = ToArgument { float =>
    new Argument(Seq(float))
  }

  implicit val doubleToArgument: ToArgument[Double] = ToArgument { double =>
    new Argument(Seq(double))
  }

  implicit val throwableToArgument: ToArgument[Throwable] = ToArgument { e =>
    new Argument(Seq(e))
  }
}

object ToArgument extends LowPriorityToArgumentImplicits {
  def apply[T: NotNothing](f: T => Argument): ToArgument[T] = new ToArgument[T] {
    override def toArgument(instance: => T): Argument = f(instance)
  }
}