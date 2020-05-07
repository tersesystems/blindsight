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

import com.tersesystems.blindsight.api._
import net.logstash.logback.argument._
import net.logstash.logback.argument.StructuredArguments._
import sourcecode.{Args, Enclosing, File, Line}

import scala.collection.JavaConverters._

/**
 * This trait contains type class instances for tuples, maps and strings that convert
 * them into StructuredArgument and add them as Arguments.
 */
trait LogstashToArgumentsImplicits {

  implicit val structuredArgToArguments: ToArgument[StructuredArgument] = ToArgument { instance =>
    new Argument(instance)
  }

  implicit val stringPairToArguments: ToArgument[(String, String)] = ToArgument {
    case (k, v) =>
      Argument(StructuredArguments.keyValue(k, v))
  }

  implicit val booleanPairToArguments: ToArgument[(String, Boolean)] = ToArgument {
    case (k, v) =>
      Argument(StructuredArguments.keyValue(k, v))
  }

  implicit def numericToArguments[T: Numeric]: ToArgument[(String, T)] = ToArgument {
    case (k, v) =>
      Argument(StructuredArguments.keyValue(k, v))
  }
}

/**
 * This trait contains type class instances that convert sourcecode structures and provide them
 * as StructuredArguments.
 */
trait SourceCodeToArgumentsImplicits extends LogstashToArgumentsImplicits {

  implicit val fileToArguments: ToArgument[File] = ToArgument { file =>
    Argument(kv("source.file", file.value))
  }

  implicit val lineToArguments: ToArgument[Line] = ToArgument { line =>
    Argument(kv("source.line", line.value))
  }

  implicit val enclosingToArguments: ToArgument[Enclosing] = ToArgument { enclosing =>
    Argument(kv("source.enclosing", enclosing.value))
  }

  implicit val argsToArguments: ToArgument[Args] = ToArgument[Args] { sourceArgs =>
    Argument {
      val args: Map[String, Any] =
        sourceArgs.value.flatMap(_.map(a => a.source -> a.value)).toMap
      kv("source.arguments", StructuredArguments.entries(args.asJava))
    }
  }
}

trait ToArgumentsImplicits extends LogstashToArgumentsImplicits with SourceCodeToArgumentsImplicits

object ToArgumentsImplicits extends ToArgumentsImplicits
