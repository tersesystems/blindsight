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
      case short: Short => BInt(short)
      case int: Int     => BInt(int)
      case l: Long      => BLong(l)
      case f: Float     => BDecimal(f)
      case d: Double    => BDouble(d)
      case b: Boolean   => BBool(b)
      case arr: BArray  => arr
      case obj: BObject => obj
      case None         => BNull
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
