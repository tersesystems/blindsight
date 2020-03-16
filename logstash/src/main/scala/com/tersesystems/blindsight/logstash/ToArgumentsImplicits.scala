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

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api.{Arguments, ToArguments}
import net.logstash.logback.argument.StructuredArguments._
import net.logstash.logback.argument.{StructuredArgument, StructuredArguments}
import sourcecode.{Args, Enclosing, File, Line}
import scala.collection.JavaConverters._

/**
 * This trait contains type class instances for tuples, maps and strings that convert
 * them into StructuredArgument and add them as Arguments.
 */
trait LogstashToArgumentsImplicits {
  implicit val argToArguments: ToArguments[StructuredArgument] = ToArguments { instance =>
    new Arguments(Seq(instance))
  }

  implicit val kvStringToArguments: ToArguments[(String, String)] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.keyValue(k, v))
  }

  implicit val kvBooleanToArguments: ToArguments[(String, Boolean)] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.keyValue(k, v))
  }

  implicit def numericToArguments[T: Numeric]: ToArguments[(String, T)] = ToArguments {
    case (k, v) =>
      Arguments(StructuredArguments.keyValue(k, v))
  }

  implicit def arrayToArguments[T <: java.lang.Object]: ToArguments[(String, Seq[T])] =
    ToArguments {
      case (k, v) =>
        Arguments(StructuredArguments.array(k, v: _*))
    }

  implicit def mapToArguments[T]: ToArguments[Map[String, T]] = ToArguments { inputMap =>
    // Maps are problematic as they export in "{a=b}" format, and not the logfmt "a=b" format used by
    // other systems.  So let's break this down in to kv pairs instead.
    inputMap.foldLeft(Arguments.empty) { (acc, el) =>
      acc + Arguments(StructuredArguments.keyValue(el._1, el._2))
    }
  }

  implicit def stringMapToArguments[T]: ToArguments[(String, Map[String, T])] =
    ToArguments {
      case (k, instance) =>
        import net.logstash.logback.argument._
        Arguments(StructuredArguments.kv(k, instance))
    }

}

/**
 * This trait contains type class instances that convert sourcecode structures and provide them
 * as StructuredArguments.
 */
trait SourceCodeToArgumentsImplicits extends LogstashToArgumentsImplicits {

  implicit val fileToArguments: ToArguments[File] = ToArguments { file =>
    Arguments(kv("source.file", file.value))
  }

  implicit val lineToToArguments: ToArguments[Line] = ToArguments { line =>
    Arguments(kv("source.line", line.value))
  }

  implicit val enclosingToArguments: ToArguments[Enclosing] = ToArguments { enclosing =>
    Arguments(kv("source.enclosing", enclosing.value))
  }

  implicit val argsToArguments: ToArguments[Args] = ToArguments[Args] { sourceArgs =>
    Arguments {
      val args: Map[String, Any] =
        sourceArgs.value.flatMap(_.map(a => a.source -> a.value)).toMap
      kv("source.arguments", StructuredArguments.entries(args.asJava))
    }
  }
}

trait ToArgumentsImplicits extends LogstashToArgumentsImplicits with SourceCodeToArgumentsImplicits

object ToArgumentsImplicits extends ToArgumentsImplicits
