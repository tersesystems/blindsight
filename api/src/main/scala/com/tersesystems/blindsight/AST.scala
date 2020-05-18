package com.tersesystems.blindsight

object AST {
  type BField = (String, BValue)

  object BValue

  sealed abstract class BValue extends Product with Serializable {
    type Values

    def values: Values

    def children: List[BValue] = this match {
      case BObject(l) => l map (_._2)
      case BArray(l)  => l
      case _          => Nil
    }

    def apply(i: Int): BValue = BNothing

    def ++(other: BValue) = {
      def append(value1: BValue, value2: BValue): BValue = (value1, value2) match {
        case (BNothing, x)            => x
        case (x, BNothing)            => x
        case (BArray(xs), BArray(ys)) => BArray(xs ::: ys)
        case (BArray(xs), v: BValue)  => BArray(xs ::: List(v))
        case (v: BValue, BArray(xs))  => BArray(v :: xs)
        case (x, y)                   => BArray(x :: y :: Nil)
      }
      append(this, other)
    }

    def toOption: Option[BValue] = this match {
      case BNothing | BNull => None
      case json             => Some(json)
    }

    def toSome: Option[BValue] = this match {
      case BNothing => None
      case json     => Some(json)
    }
  }

  case object BNothing extends BValue {
    type Values = None.type
    def values = None
  }
  case object BNull extends BValue {
    type Values = Null
    def values = null
  }
  final case class BString(s: String) extends BValue {
    type Values = String
    def values = s
  }
  trait BNumber
  final case class BDouble(num: Double) extends BValue with BNumber {
    type Values = Double
    def values = num
  }
  final case class BDecimal(num: BigDecimal) extends BValue with BNumber {
    type Values = BigDecimal
    def values = num
  }
  final case class BLong(num: Long) extends BValue with BNumber {
    type Values = Long
    def values = num
  }
  final case class BInt(num: BigInt) extends BValue with BNumber {
    type Values = BigInt
    def values = num
  }

  final case class BBool(value: Boolean) extends BValue {
    type Values = Boolean
    def values = value
  }

  object BBool {
    val True  = new BBool(true)
    val False = new BBool(false)
  }

  final case class BObject(obj: List[BField]) extends BValue {
    type Values = Map[String, Any]
    def values: Map[String, Any] = obj.iterator.map { case (n, v) => (n, v.values) }.toMap

    override def equals(that: Any): Boolean = that match {
      case o: BObject => obj.toSet == o.obj.toSet
      case _          => false
    }

    override def hashCode: Int = obj.toSet[BField].hashCode
  }

  final case class BArray(arr: List[BValue]) extends BValue {
    type Values = List[Any]
    def values                         = arr.map(_.values)
    override def apply(i: Int): BValue = arr(i)
  }

  // BSet is set implementation for BSet.
  // It supports basic set operations, like intersection, union and difference.
  final case class BSet(set: Set[BValue]) extends BValue {
    type Values = Set[BValue]
    def values: Set[BValue] = set

    override def equals(o: Any): Boolean = o match {
      case o: BSet => o.values == values
      case _       => false
    }

    def intersect(o: BSet): BSet  = BSet(o.values.intersect(values))
    def union(o: BSet): BSet      = BSet(o.values.union(values))
    def difference(o: BSet): BSet = BSet(values.diff(o.values))
  }

  object BField {
    def apply(name: String, value: BValue): (String, BValue) = (name, value)

    final class OptionStringBValue(val get: BField) extends AnyVal {
      def isEmpty: Boolean = false
    }
    def unapply(f: BField): OptionStringBValue = new OptionStringBValue(f)
  }
}

object bodj {
  def apply(fs: AST.BField*): AST.BObject = AST.BObject(fs.toList)
}
