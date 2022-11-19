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

import com.tersesystems.blindsight.AST.{BArray, BObject}

/**
 * This type class is used to create [[Value]] instances from
 * given types.
 *
 * There are types provided for all the JSON-LD primitives and typed values.
 *
 * See [[ValueBinding]] for example usage
 *
 * @tparam T the type class instance.
 */
trait ValueMapper[T] {
  def mapValue(value: T): Value[_]
}

object ValueMapper {

  def apply[T](f: T => Value[_]): ValueMapper[T] =
    new ValueMapper[T]() {
      override def mapValue(value: T): Value[_] = f(value)
    }

  implicit val stringLiteralMapper: ValueMapper[String] =
    ValueMapper(v => StringLiteral(v))

  implicit val shortLiteralMapper: ValueMapper[Short] =
    ValueMapper(v => NumberLiteral(v))

  implicit val intLiteralMapper: ValueMapper[Int] =
    ValueMapper(v => NumberLiteral(v))

  implicit val longLiteralMapper: ValueMapper[Long] =
    ValueMapper(v => NumberLiteral(v))

  implicit val floatLiteralMapper: ValueMapper[Float] =
    ValueMapper(v => NumberLiteral(v))

  implicit val doubleLiteralMapper: ValueMapper[Double] =
    ValueMapper(v => NumberLiteral(v))

  implicit val booleanLiteralMapper: ValueMapper[Boolean] =
    ValueMapper(v => BooleanLiteral(v))

  implicit val bobjectLiteralMapper: ValueMapper[BObject] =
    ValueMapper(obj => JsonObjectLiteral(obj))

  implicit val barrayLiteralMapper: ValueMapper[BArray] =
    ValueMapper(arr => JsonArrayLiteral(arr))

  // A typed value or any kind of value passed in gets passed in
  implicit def identityMapper[V <: Value[_]]: ValueMapper[V] = ValueMapper(identity)

  // If you want a null, you have to use an Option value and pass a None through.
  implicit def optValueMapper[V: ValueMapper]: ValueMapper[Option[V]] =
    ValueMapper {
      case Some(v) => Value(v)
      case None    => Value.none
    }
}

/**
 * This type class is used to create [[NodeObject]] instances from
 * given types.
 *
 * See [[NodeObjectBinding]] for example usage.
 *
 * @tparam T the type to convert to a [[NodeObject]]
 */
trait NodeObjectMapper[T] {
  def mapNodeObject(value: T): NodeObject
}

object NodeObjectMapper {
  def apply[T](f: T => NodeObject): NodeObjectMapper[T] =
    new NodeObjectMapper[T] {
      override def mapNodeObject(value: T): NodeObject = f(value)
    }

  implicit val identityMapper: NodeObjectMapper[NodeObject] = NodeObjectMapper(identity)

  implicit def optNodeObject[T: NodeObjectMapper]: NodeObjectMapper[Option[T]] =
    NodeObjectMapper {
      case Some(v) =>
        implicitly[NodeObjectMapper[T]].mapNodeObject(v)
      case None =>
        NodeObject.Null
    }

}

/**
 * This type class allows things that are not Terms/LongIRI/CompactIRI to be mapped.
 *
 * See [[IRIBinding]] for example usage.
 *
 * @tparam T the type class instance
 */
trait IRIValueMapper[T] {
  def mapIRIValue(value: T): IRIValue
}

object IRIValueMapper {
  def apply[T](f: T => IRIValue): IRIValueMapper[T] =
    new IRIValueMapper[T] {
      override def mapIRIValue(value: T): IRIValue = f(value)
    }

  implicit val identityMapper: IRIValueMapper[IRIValue] = IRIValueMapper(identity)

  implicit def valueMapper[T <: IRIValue]: IRIValueMapper[T] =
    IRIValueMapper[T] { instance => IRI(instance.value) }
}

/**
 * This type class is used to map elements to a [[Node]] automatically.
 *
 * Normally you won't need to implement this type class directly.  It
 * is used by the list and set mappers to convert other type class instances
 * into [[Node]].
 *
 * @tparam T the type class instance.
 */
trait NodeMapper[T] {
  def mapNode(value: T): Node
}

object NodeMapper {
  def apply[T](f: T => Node): NodeMapper[T] =
    new NodeMapper[T] {
      override def mapNode(value: T): Node = f(value)
    }

  implicit def optionMapperToNodeMapper[T: NodeMapper]: NodeMapper[Option[T]] =
    NodeMapper {
      case Some(v) => implicitly[NodeMapper[T]].mapNode(v)
      case None    => NullLiteral
    }

  implicit def valueMapperToNodeMapper[T: ValueMapper]: NodeMapper[T] =
    NodeMapper[T] { value =>
      implicitly[ValueMapper[T]].mapValue(value)
    }

  implicit def nodeObjectMapperToNodeMapper[T: NodeObjectMapper]: NodeMapper[T] =
    NodeMapper[T] { value =>
      implicitly[NodeObjectMapper[T]].mapNodeObject(value)
    }

  implicit def iriValueMapperToNodeMapper[T: IRIValueMapper]: NodeMapper[T] =
    NodeMapper[T] { value =>
      implicitly[IRIValueMapper[T]].mapIRIValue(value)
    }
}
