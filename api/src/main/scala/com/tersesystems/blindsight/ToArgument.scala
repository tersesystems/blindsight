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

/**
 * This is a type class used to convert given types to [[Argument]].
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
trait ToArgument[T] {
  def toArgument(instance: T): Argument
}

object LowPriorityToArgumentImplicits {
  private val unitArgument = new Argument(())
}

trait LowPriorityToArgumentImplicits {

  implicit val argumentToArgument: ToArgument[Argument] = ToArgument(identity)

  implicit def throwableToArgument[E <: Throwable]: ToArgument[E] = ToArgument { throwable => new Argument(throwable) }

  implicit val unitToArguments: ToArgument[Unit] = ToArgument { _ => LowPriorityToArgumentImplicits.unitArgument }

  implicit val stringToArgument: ToArgument[String] = ToArgument { string => new Argument(string) }

  implicit val booleanToArgument: ToArgument[Boolean] = ToArgument { bool => new Argument(bool) }

  implicit val shortToArgument: ToArgument[Short] = ToArgument { short => new Argument(short) }

  implicit val intToArgument: ToArgument[Int] = ToArgument { int => new Argument(int) }

  implicit val longToArgument: ToArgument[Long] = ToArgument { long => new Argument(long) }

  implicit val floatToArgument: ToArgument[Float] = ToArgument { float => new Argument(float) }

  implicit val doubleToArgument: ToArgument[Double] = ToArgument { double => new Argument(double) }

  implicit val bobjectToArgument: ToArgument[BObject] = ToArgument { bobject =>
    ArgumentResolver(bobject)
  }
}

object ToArgument extends LowPriorityToArgumentImplicits {
  def apply[T: NotNothing](f: T => Argument): ToArgument[T] =
    new ToArgument[T] {
      override def toArgument(instance: T): Argument = f(instance)
    }
}

/**
 * This trait allows a type that has a [[ToArgument]] type class instance to render as an [[Argument]].
 *
 * This is especially useful in the SLF4J API, which does not take a [[ToMarkers]] instance.
 *
 * {{{
 * import ArgumentEnrichment._
 * val argument: Argument = myInstance.asArgument
 * }}}
 */
trait ArgumentEnrichment {
  implicit class RichToArgument[A: ToArgument](instance: A) {
    def asArgument: Argument = implicitly[ToArgument[A]].toArgument(instance)
  }
}

object ArgumentEnrichment extends ArgumentEnrichment
