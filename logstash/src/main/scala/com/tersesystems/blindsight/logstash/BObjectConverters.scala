package com.tersesystems.blindsight.logstash

object BObjectConverters {
  import scala.collection.JavaConverters._
  import com.tersesystems.blindsight.api.AST._

  def asJava(bobj: BObject): java.util.Map[String, Any] = {
    bobj.obj
      .map {
        case (k, v) =>
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
