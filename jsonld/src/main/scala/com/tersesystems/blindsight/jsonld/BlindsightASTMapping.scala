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

package com.tersesystems.blindsight.jsonld

import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.bobj

/**
 * Maps the root NodeObject to a BObject.
 */
trait BlindsightASTMapping {

  /**
   * Creates a BObject from a NodeObject.
   *
   * @param nodeObject the input
   * @return the output
   */
  def toBObject(nodeObject: NodeObject): BObject = {
    val bfields = extractNodeObject(nodeObject)
    bobj(bfields: _*)
  }

  private def extractTypedValue(tv: TypedValue): BValue = {
    bobj(
      Keyword.`@value`.name -> BString(tv.value),
      Keyword.`@type`.name  -> BString(tv.valueType.name)
    )
  }

  private def extractLiteralValue(vo: Value[_]): BValue = {
    vo.value match {
      case s: String    => BString(s)
      case short: Short => BInt(short.toInt)
      case int: Int     => BInt(int)
      case l: Long      => BLong(l)
      case f: Float     => BDecimal(new java.math.BigDecimal(f))
      case d: Double    => BDouble(d)
      case b: Boolean   => BBool(b)
      case arr: BArray  => arr
      case obj: BObject => obj
      case None         => BNull
      case other =>
        throw new IllegalStateException(s"other = $other")
    }
  }

  def extractStringValue(sv: StringValue): BValue = {
    import com.tersesystems.blindsight.DSL._
    val `@direction` = Keyword.`@direction`.name
    val `@language`  = Keyword.`@language`.name
    val `@value`     = Keyword.`@value`.name
    sv match {
      case StringValue(value, Some(directionValue), Some(lang)) =>
        bobj(
          `@value`     -> value,
          `@direction` -> directionValue.value,
          `@language`  -> lang
        )
      case StringValue(value, None, Some(lang)) =>
        bobj(
          `@value`    -> value,
          `@language` -> lang
        )
      case StringValue(value, Some(directionValue), None) =>
        bobj(
          `@value`     -> value,
          `@direction` -> directionValue.value
        )
      case StringValue(value, None, None) =>
        BString(value)
    }
  }

  private def extractListObject(listObject: ListObject): BValue = {
    val bvalues = listObject.value.map(extractJsonValue)
    // https://w3c.github.io/json-ld-syntax/#lists
    bobj(Keyword.`@list`.name -> BArray(bvalues.toList))
  }

  private def extractSetObject(setObject: SetObject): BValue = {
    val value = setObject.value.map(extractJsonValue)
    // https://w3c.github.io/json-ld-syntax/#sets
    bobj(Keyword.`@set`.name -> BArray(value.toList))
  }

  private def extractJsonValue(el: Node): BValue = {
    el match {
      case sv: StringValue                         => extractStringValue(sv)
      case tv: TypedValue                          => extractTypedValue(tv)
      case nv: LiteralValue[_]                     => extractLiteralValue(nv)
      case no: NodeObject if no == NodeObject.Null => BNull
      case no: NodeObject                          => BObject(extractNodeObject(no).toList)
      case lo: ListObject                          => extractListObject(lo)
      case so: SetObject                           => extractSetObject(so)
      case other => throw new IllegalStateException(s"other = $other")
    }
  }

  private def extractNodeObject(no: NodeObject): Seq[BField] = {
    no.value.map { entry =>
      entry.value match {
        case Seq() =>
          BField(entry.key, BNull)
        case Seq(el) =>
          val jsonValue = extractJsonValue(el)
          BField(entry.key, jsonValue)
        case nodes =>
          val seqValue = nodes.map(extractJsonValue).toList
          BField(entry.key, BArray(seqValue))
      }
    }
  }

}

object BlindsightASTMapping extends BlindsightASTMapping
