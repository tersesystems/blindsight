package com.tersesystems.blindsight

import com.tersesystems.blindsight.AST._

import scala.language.implicitConversions

trait BigDecimalMode { self: DSLImplicits =>
  implicit def double2BValue(x: Double): BValue         = BDecimal(x)
  implicit def float2BValue(x: Float): BValue           = BDecimal(x.toDouble)
  implicit def bigdecimal2BValue(x: BigDecimal): BValue = BDecimal(x)
}
object BigDecimalMode extends DSLImplicits with BigDecimalMode

trait DoubleMode { self: DSLImplicits =>
  implicit def double2BValue(x: Double): BValue         = BDouble(x)
  implicit def float2BValue(x: Float): BValue           = BDouble(x.toDouble)
  implicit def bigdecimal2BValue(x: BigDecimal): BValue = BDouble(x.doubleValue)
}
object DoubleMode extends DSLImplicits with DoubleMode

trait DSLImplicits {
  implicit def short2BValue(x: Short): BValue   = BInt(x.toInt)
  implicit def byte2BValue(x: Byte): BValue     = BInt(x.toInt)
  implicit def char2BValue(x: Char): BValue     = BInt(x.toInt)
  implicit def int2BValue(x: Int): BValue       = BInt(x)
  implicit def long2BValue(x: Long): BValue     = BInt(x)
  implicit def bigint2BValue(x: BigInt): BValue = BInt(x)
  implicit def double2BValue(x: Double): BValue
  implicit def float2BValue(x: Float): BValue
  implicit def bigdecimal2BValue(x: BigDecimal): BValue
  implicit def boolean2BValue(x: Boolean): BValue = BBool(x)
  implicit def string2BValue(x: String): BValue   = BString(x)
}

/**
 * A DSL to produce valid tree of values.
 * Example:<pre>
 * import BlindsightDSL._
 * ("name", "joe") ~ ("age", 15) == BObject(BField("name",BString("joe")) :: BField("age",BInt(15)) :: Nil)
 * </pre>
 */
object DSL extends DSL with DoubleMode {
  object WithDouble     extends DSL with DoubleMode
  object WithBigDecimal extends DSL with BigDecimalMode
}

trait DSL extends DSLImplicits {
  implicit def seq2BValue[A](s: Iterable[A])(implicit ev: A => BValue): BArray =
    BArray(s.toList.map { a => val v: BValue = a; v })

  implicit def map2BValue[A](m: Map[String, A])(implicit ev: A => BValue): BObject =
    BObject(m.toList.map { case (k, v) => BField(k, v) })

  implicit def option2BValue[A](opt: Option[A])(implicit ev: A => BValue): BValue = opt match {
    case Some(x) => x
    case None    => BNothing
  }

  implicit def symbol2BValue(x: Symbol): BString = BString(x.name)
  implicit def pair2BValue[A](t: (String, A))(implicit ev: A => BValue): BObject =
    BObject(List(BField(t._1, t._2)))
  implicit def list2BValue(l: List[BField]): BObject    = BObject(l)
  implicit def BObject2assoc(o: BObject): JsonListAssoc = new JsonListAssoc(o.obj)
  implicit def pair2Assoc[A](t: (String, A))(implicit ev: A => BValue): JsonAssoc[A] =
    new JsonAssoc(t)

  class JsonAssoc[A](left: (String, A))(implicit ev: A => BValue) {
    def ~[B](right: (String, B))(implicit ev1: B => BValue): BObject = {
      val l: BValue = left._2
      val r: BValue = right._2
      BObject(BField(left._1, l) :: BField(right._1, r) :: Nil)
    }

    def ~(right: BObject): BObject = {
      val l: BValue = left._2
      BObject(BField(left._1, l) :: right.obj)
    }
    def ~~[B](right: (String, B))(implicit ev: B => BValue): BObject = this.~(right)
    def ~~(right: BObject): BObject                                  = this.~(right)

  }

  class JsonListAssoc(left: List[BField]) {
    def ~(right: (String, BValue)): BObject  = BObject(left ::: List(BField(right._1, right._2)))
    def ~(right: BObject): BObject           = BObject(left ::: right.obj)
    def ~~(right: (String, BValue)): BObject = this.~(right)
    def ~~(right: BObject): BObject          = this.~(right)
  }
}
