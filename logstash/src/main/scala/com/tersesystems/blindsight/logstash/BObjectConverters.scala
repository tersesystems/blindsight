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

package com.tersesystems.blindsight.logstash

object BObjectConverters {
  import com.tersesystems.blindsight.AST._

  import scala.jdk.CollectionConverters._

  def asJava(bobj: BObject): java.util.Map[String, Any] = {
    bobj.obj
      .map { case (k, v) =>
        val javaValue = bvalueToJava(v)
        k -> javaValue
      }
      .toMap
      .asJava
  }

  private def arrayToJava(barry: BArray): Any = {
    barry.children.map(bvalueToJava).asJavaCollection
  }

  private def bvalueToJava(bvalue: BValue): Any = {
    bvalue match {
      case bobj: BObject =>
        asJava(bobj)
      case barr: BArray =>
        arrayToJava(barr)
      case BSet(set) =>
        set.map(bvalueToJava).asJavaCollection
      case b: BBool =>
        b.value
      case BString(s) =>
        s
      case n: BNumber =>
        n.values
      case BNull =>
        null
      case BNothing =>
        null
    }
  }
}
