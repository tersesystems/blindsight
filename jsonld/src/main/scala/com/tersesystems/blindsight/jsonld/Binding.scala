package com.tersesystems.blindsight.jsonld

import scala.collection.immutable.Iterable

/**
 * A binding represents a mapping between a [[BindingKey]] and a [[Node]] of
 * some specific type.
 *
 * Bindings come from a [[Keyword]] or an [[IRIValue]], using a `bindFoo` style,
 * i.e. an [[IRIBinding]] is returned if `bindIRI` is used.  Different bindings
 * are possible on different keywords.
 *
 * Most of the documentation and exmaples are in the implementations.
 */
trait Binding {
  def key: BindingKey
}

/**
 * A binding of a key to an [[IRIValue]], returning a [[NodeEntry]].
 *
 * This is most commonly used with keywords, i.e. `@type`:
 *
 * {{{
 * val `@type`   = Keyword.`@type`.bindIRI
 * val givenName  = schemaOrg("givenName").bindValue[String]
 * val personType = schemaOrg("Person")
 * val willPerson = NodeObject(
 *   `@type` -> personType,
 *   givenName -> "Will"
 * )
 * }}}
 *
 * A custom [[IRIValueMapper]] can be defined for a type.  This can
 * be particularly useful when referring by id:
 *
 * {{{
 * implicit val personIRIMapper: IRIValueMapper[Person] = IRIValueMapper(p => IRI(p.id))
 *
 * val id = exampleOrg("president").bindIRI[Person] // has @type -> @id
 * val obama = Person("http://www.wikidata.org/entity/Q76", name = "Barack Obama")
 * val america = NodeObject(
 *   president -> obama
 * )
 * }}}
 */
final case class IRIBinding[T: IRIValueMapper](key: IRIBindingKey) extends Binding {

  /**
   * Alias for `bind`.
   *
   * @param value
   * @return
   */
  def ->(value: T): NodeEntry = bind(value)

  /**
   * @param value
   * @return
   */
  def bind(value: T): NodeEntry = {
    val mapper = implicitly[IRIValueMapper[T]]
    NodeEntry(key.name, StringLiteral(mapper.mapIRIValue(value).name))
  }

}

final case class IRIsBinding[T: IRIValueMapper](key: IRIBindingKey) extends Binding {

  /**
   * Alias for `bind`.
   *
   * @param value
   * @return
   */
  def ->(value: scala.Iterable[T]): NodeEntry = bind(value)

  /**
   * @param value
   * @return
   */
  def bind(values: scala.Iterable[T]): NodeEntry = {
    val mapper = implicitly[IRIValueMapper[T]]
    val seq    = values.map(value => StringLiteral(mapper.mapIRIValue(value).name)).toSeq
    NodeEntry(key.name, seq: _*)
  }

}

/**
 * A binding between a key that is a "JSON-LD value" and a Scala type that can be
 * represented as that value. A [[ValueMapper]] is responsible for doing the conversion.
 *
 * You can define your own [[ValueMapper]] for custom types, and primitives
 * (String, Int, etc) are already defined:
 *
 * {{{
 * val givenName  = schemaOrg("givenName").bindValue[String]
 * val willPerson = NodeObject(
 *   givenName    -> "Person"
 * )
 * }}}
 *
 * Using a "null" is done by binding to an Option[T] of the type you want:
 *
 * {{{
 * val givenName  = schemaOrg("givenName").bindValue[Option[String]]
 * val willPerson = NodeObject(
 *   givenName  -> None
 * )
 * }}}
 *
 * Some JSON-LD values are represented as "value objects", notably strings
 * with languages and typed values.
 *
 * To represent a string value:
 *
 * {{{
 * val givenName  = schemaOrg("givenName").bindValue[String]
 * val willPerson = NodeObject(
 *   givenName -> Value("Will", StringDirection.LeftToRight, "en"),
 * )
 * }}}
 *
 * You can require string values over literals by binding explicitly:
 *
 * {{{
 * val givenName  = schemaOrg("givenName").bindValue[StringValue]
 * }}}
 *
 * Likewise typed values are usually converted but can be defined
 * explicitly:
 *
 * {{{
 * val name        = schemaOrg("name").bindValue[String]
 * val dateCreated = schemaOrg("dateCreated").bindValue[TypedValue]
 * val localDate = LocalDate.of(2020, 1, 1)
 * val abridgedMobyDick = NodeObject(
 *   `@type`     -> "Book",
 *   name        -> "Moby Dick",
 *   dateCreated -> Value(DateTimeFormatter.ISO_DATE.format(localDate), xsdDate)
 * )
 * }}}
 *
 * You can map any type to a value using a custom [[ValueMapper]]:
 *
 * {{{
 * val `@type` = Keyword.`@type`.bindIRI
 * val schemaOrg: Vocab        = IRI("https://schema.org/").vocab
 * val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
 * val value: ValueBinding[Int]   = schemaOrg("value").bindValue[Int]
 *
 * implicit val currencyMapper: ValueMapper[Currency] = ValueMapper { currency =>
 *   Value(currency.getCurrencyCode)
 * }
 * val currency: ValueBinding[Currency] = schemaOrg("currency").bindValue[Currency]
 * }}}
 *
 * @param key the value binding key
 * @tparam T the type which has a [[ValueMapper]] type class instance
 */
final case class ValueBinding[T: ValueMapper](key: ValueBindingKey) extends Binding {

  /**
   * Alias for bind.
   *
   * @param value the value
   * @return the node entry
   */
  def ->(value: T): NodeEntry = bind(value)

  /**
   * Binds to a value of T.
   *
   * @param value the value
   * @return the node entry
   */
  def bind(value: T): NodeEntry = {
    val mapper = implicitly[ValueMapper[T]]
    NodeEntry(key.name, mapper.mapValue(value))
  }

  /**
   * Binds to a value of T.
   *
   * @param value the value
   * @return the node entry
   */
  def bind(value: Value[T]): NodeEntry = {
    NodeEntry(key.name, value)
  }

  /**
   * Binds to a value of T.
   *
   * @param value the value
   * @return the node entry
   */
  def ->(value: Value[T]): NodeEntry = bind(value)

  override def toString: String = key.toString
}

/**
 * Used when you want to return an array of values but they are not list objects.
 */
final case class ValueArrayBinding[T: NodeMapper](key: BindingKey) extends Binding {

  def bind(values: scala.Iterable[T]): NodeEntry = {
    val mapper = implicitly[NodeMapper[T]]
    val seq    = values.map(el => mapper.mapNode(el)).toSeq
    NodeEntry(key.name, seq: _*)
  }

  def ->(values: scala.Iterable[T]): NodeEntry = bind(values)

  def bind(values: Iterable[Value[T]]): NodeEntry = {
    val seq: Seq[Value[T]] = values.toSeq
    NodeEntry(key.name, seq: _*)
  }

  /**
   * Binds to a value of T.
   *
   * @param value the value
   * @return the node entry
   */
  def ->(value: Iterable[Value[T]]): NodeEntry = bind(value)
}

/**
 * A binding between a key that is a "JSON-LD value" and a Scala type that can be
 * represented as that value.  A [[NodeObjectMapper]] is responsible for doing the conversion.
 *
 * Most of the time you'll use a custom NodeObjectMapper to convert appropriately, building on
 * other bindings in your context.  For example, to render a `MonetaryAmount` class as a node
 * object, you can add the binding in a context that contains `currency` and `value` bindings:
 *
 * {{{
 * case class MonetaryAmount(currency: Currency, value: Int)
 *
 * /** Represent as https://schema.org/MonetaryAmount instance */
 * trait MonetaryAmountMapper extends MyContext {
 *   implicit val monetaryAmountMapper: NodeObjectMapper[MonetaryAmount] = NodeObjectMapper { ma =>
 *     NodeObject(
 *       `@type`  -> monetaryAmountType,
 *       currency -> ma.currency,
 *       value    -> ma.value
 *     )
 *   }
 * }
 * object MonetaryAmountMapper extends MonetaryAmountMapper
 * }}}
 *
 * And then you can use `bindObject[MonetaryAmount]`.
 *
 * {{{
 * import MonetaryAmountMapper._
 *
 * val estimatedSalary: NodeObjectBinding[MonetaryAmount] =
 *  schemaOrg("estimatedSalary").bindObject[MonetaryAmount]
 * }}}
 *
 * You can also bind a simple [[NodeObject]] without mapping.  For example,
 * you can represent an organization with an employee as a raw [[NodeObject]]:
 *
 * {{{
 * val employee  = schemaOrg("employee").bindObject[NodeObject]
 * val organization = NodeObject(
 *   `@type`   -> schemaOrg("Organization")
 *   name -> "Terse Systems",
 *   employee -> NodeObject(
 *     `@type`   -> schemaOrg("Person")
 *     givenName -> "Will",
 *   )
 * )
 * }}}
 */
final case class NodeObjectBinding[T: NodeObjectMapper](key: BindingKey) extends Binding {

  /**
   * Binds to a value of T.
   *
   * @param value the value
   * @return the node entry
   */
  def bind(value: T): NodeEntry = {
    NodeEntry(key.name, implicitly[NodeObjectMapper[T]].mapNodeObject(value))
  }

  /**
   * Binds to a value of T.
   *
   * @param value the value
   * @return the node entry
   */
  def ->(value: T): NodeEntry = bind(value)
}

/**
 * A binding between a list object and a value.
 *
 * {{{
 * val listOfStrings = schemaOrg("listOfStrings").bindList[String]
 *
 * val aListOfStrings = NodeObject(
 *  listOfStrings bind Seq("One", "Two", "Three", "Four")
 * )
 * }}}
 *
 * You can resolve elements as `null` using an `Option` type:
 *
 * {{{
 * val optionalStrings = schemaOrg("optionalStrings").bindList[Option[String]]
 * val obj = NodeObject(
 *  optionalStrings bind Seq(
 *    Some("some"),
 *    None,
 *  )
 * )
 * }}}
 *
 * You can specify a list object to contain other list objects.  For example, using the
 * <a href="https://w3c.github.io/json-ld-syntax/#example-83-coordinates-expressed-in-geojson">geometry example</a>:
 *
 * {{{
 *
 * trait MyGeoContext {
 *  val geojson   = IRI("https://purl.org/geojson/vocab#")
 *  val bbox    = geojson.term("bbox").bindList[Double]
 *  val geometry = geojson.term("geometry").bindObject[Geometry]
 *
 *  implicit def seqMapper: NodeMapper[Seq[Double]] =
 *    NodeMapper { iter =>
 *      val mapper = implicitly[NodeMapper[Double]]
 *      ListObject(iter.map(mapper.mapNode))
 *    }
 *
 *  val coordinates = vocab.term("coordinates").bindList[Seq[Double]]
 * }
 *
 * final case class Geometry(`@type`: String, coords: Seq[Seq[Double]])
 *
 * object Geometry extends MyGeoContext {
 *  implicit val nodeMapper: NodeObjectMapper[Geometry] = NodeObjectMapper { geo =>
 *    val `@type`   = Keyword.`@type`.bindIRI
 *    NodeObject(
 *      `@type`     -> geo.`@type`,
 *      coordinates -> geo.coords
 *    )
 *  }
 * }
 *
 * val geoFeatureNode = NodeObject(
 *  `@type` -> "Feature",
 *  bbox    -> Seq(-10.0, -10.0, 10.0, 10.0),
 *  geometry -> Geometry(
 *    "polygon",
 *    Seq(
 *      Seq(-10.0, -10.0),
 *      Seq(10.0, -10.0),
 *      Seq(10.0, 10.0),
 *      Seq(-10.0, 10.0)
 *    )
 *  )
 * )
 * }}}
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#lists">Lists</a>
 * @param key the binding key
 *  @tparam T the node mapper type class instance
 */
final case class ListBinding[T: NodeMapper](key: BindingKey) extends Binding {

  def bind(value: scala.Iterable[T]): NodeEntry = {
    val mapper = implicitly[NodeMapper[T]]
    NodeEntry(key.name, ListObject(value.map(v => mapper.mapNode(v))))
  }

  def ->(value: scala.Iterable[T]): NodeEntry = bind(value)
}

/**
 * TODO fill this out
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#sets">Sets</a>
 *
 * @param key the binding key
 * @tparam T the node mapper type class instance
 */
final case class SetBinding[T: NodeMapper](key: BindingKey) extends Binding {

  def bind(value: scala.Iterable[T]): NodeEntry = {
    val mapper = implicitly[NodeMapper[T]]
    NodeEntry(key.name, SetObject(value.map(v => mapper.mapNode(v))))
  }

  def ->(value: scala.Iterable[T]): NodeEntry = bind(value)
}

/**
 * Used when you want to return an array of values but they are not list objects.
 */
final case class NodeObjectArrayBinding[T: NodeMapper](key: BindingKey) extends Binding {

  def bind(values: scala.Iterable[T]): NodeEntry = {
    val mapper = implicitly[NodeMapper[T]]
    val seq    = values.map(el => mapper.mapNode(el)).toSeq
    NodeEntry(key.name, seq: _*)
  }

  def ->(values: scala.Iterable[T]): NodeEntry = bind(values)
}

/**
 */
trait MapBinding extends Binding

/**
 * Index map that maps node objects as values.
 *
 * Because this is an index map, the keys here are strings rather than binding keys.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#data-indexing">data indexing</a>
 *
 * @param key the binding key for the map
 * @tparam T a type class instance
 */
final case class IndexMapBinding(key: BindingKey) extends Binding {
  import IndexMapBinding.Input

  def bind(input: Input): NodeEntry = {
    NodeEntry(key.name, NodeObject(input.entries: _*))
  }

  def ->(input: Input): NodeEntry = bind(input)
}

object IndexMapBinding {
  final case class Input(input: Map[Option[String], Seq[Node]]) {
    def entries: Seq[NodeEntry] =
      input.map {
        case (Some(k), v) =>
          NodeEntry(k, v: _*)
        case (None, v) =>
          NodeEntry(Keyword.`@none`.name, v: _*)
      }.toSeq
  }

  object Input {
    implicit def toInput[T: NodeMapper](input: Map[String, T]): Input = {
      val mapper   = implicitly[NodeMapper[T]]
      val inputMap = input.map { case (k, v) => Option(k) -> Seq(mapper.mapNode(v)) }
      Input(inputMap)
    }

    implicit def optToInput[T: NodeMapper](input: Map[Option[String], T]): Input = {
      val mapper   = implicitly[NodeMapper[T]]
      val elements = input.map { case (k, v) => k -> Seq(mapper.mapNode(v)) }
      Input(elements)
    }

    implicit def optToSeqInput[T: NodeMapper](input: Map[Option[String], Seq[T]]): Input = {
      val mapper   = implicitly[NodeMapper[T]]
      val elements = input.map { case (k, v) => k -> v.map(mapper.mapNode) }
      Input(elements)
    }
  }
}

/**
 * Index map that maps node objects as values, using property based values.
 *
 * In property-based data indexing, index maps can only be used on node objects,
 * not value objects or graph objects, so there is no `ValueMapper` equivalent.
 *
 * @param key the binding key for the map
 * @tparam T a type class instance
 */
final case class IdMapBinding(key: BindingKey) extends Binding {
  import IdMapBinding.Input

  def bind(input: Input): NodeEntry = NodeEntry(key.name, NodeObject(input.entries: _*))

  def ->(value: Input): NodeEntry = bind(value)
}

object IdMapBinding {

  final case class Input(input: Map[Option[IRIValue], Seq[NodeObject]]) {
    def entries: Seq[NodeEntry] = {
      input.map {
        case (Some(k), list) => NodeEntry(k.value, list: _*)
        case (None, list)    => NodeEntry(Keyword.`@none`.name, list: _*)
      }.toSeq
    }
  }

  object Input {
    implicit def straightMap[I: IRIValueMapper, T: NodeObjectMapper](elements: Map[I, T]): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map { case (k, v) =>
        val key   = iriMapper.mapIRIValue(k)
        val value = nodeMapper.mapNodeObject(v)
        Option(key) -> Seq(value)
      }
      Input(value)
    }

    implicit def seqInput[I: IRIValueMapper, T: NodeObjectMapper](
        elements: Map[I, Seq[T]]
    ): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map { case (k, v) =>
        val key = iriMapper.mapIRIValue(k)
        Option(key) -> v.map(el => nodeMapper.mapNodeObject(el))
      }
      Input(value)
    }

    implicit def optionKeyInput[I: IRIValueMapper, T: NodeObjectMapper](
        elements: Map[Option[I], T]
    ): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map {
        case (Some(k), v) =>
          val key = iriMapper.mapIRIValue(k)
          Option(key) -> Seq(nodeMapper.mapNodeObject(v))
        case (None, v) =>
          None -> Seq(nodeMapper.mapNodeObject(v))
      }
      Input(value)
    }

    implicit def optionKeySeqInput[I: IRIValueMapper, T: NodeObjectMapper](
        elements: Map[Option[I], Seq[T]]
    ): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map {
        case (Some(k), v) =>
          val key = iriMapper.mapIRIValue(k)
          Option(key) -> v.map(nodeMapper.mapNodeObject)
        case (None, v) =>
          None -> v.map(nodeMapper.mapNodeObject)
      }
      Input(value)
    }
  }

}

/**
 * A type map is a map value of a term defined with @container set to @type,
 * whose keys are interpreted as IRIs representing the @type of the
 * associated node object.
 *
 * <a href="https://www.w3.org/TR/json-ld11/#type-maps">Definition</a>
 *
 * The value must be a node object, or array of node objects, so there is no [[ValueMapper]]
 * equivalent here.
 *
 * @param key the binding key for the map
 * @tparam T a type class instance
 */
final case class TypeMapBinding(
    key: BindingKey
) extends Binding {
  import TypeMapBinding.Input

  def bind(value: Input): NodeEntry = {
    NodeEntry(key.name, NodeObject(value.entries: _*))
  }

  def ->(value: Input): NodeEntry = bind(value)
}

object TypeMapBinding {
  final case class Input(input: Map[Option[IRIValue], Seq[NodeObject]]) {
    def entries: Seq[NodeEntry] = {
      input.map {
        case (Some(k), list) => NodeEntry(k.value, list: _*)
        case (None, list)    => NodeEntry(Keyword.`@none`.name, list: _*)
      }.toSeq
    }
  }

  object Input {
    implicit def straightMap[I: IRIValueMapper, T: NodeObjectMapper](elements: Map[I, T]): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map { case (k, v) =>
        val key   = iriMapper.mapIRIValue(k)
        val value = nodeMapper.mapNodeObject(v)
        Option(key) -> Seq(value)
      }
      Input(value)
    }

    implicit def seqInput[I: IRIValueMapper, T: NodeObjectMapper](
        elements: Map[I, Seq[T]]
    ): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map { case (k, v) =>
        val key = iriMapper.mapIRIValue(k)
        Option(key) -> v.map(el => nodeMapper.mapNodeObject(el))
      }
      Input(value)
    }

    implicit def optionKeyInput[I: IRIValueMapper, T: NodeObjectMapper](
        elements: Map[Option[I], T]
    ): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map {
        case (Some(k), v) =>
          val key = iriMapper.mapIRIValue(k)
          Option(key) -> Seq(nodeMapper.mapNodeObject(v))
        case (None, v) =>
          None -> Seq(nodeMapper.mapNodeObject(v))
      }
      Input(value)
    }

    implicit def optionKeySeqInput[I: IRIValueMapper, T: NodeObjectMapper](
        elements: Map[Option[I], Seq[T]]
    ): Input = {
      val iriMapper  = implicitly[IRIValueMapper[I]]
      val nodeMapper = implicitly[NodeObjectMapper[T]]
      val value = elements.map {
        case (Some(k), v) =>
          val key = iriMapper.mapIRIValue(k)
          Option(key) -> v.map(nodeMapper.mapNodeObject)
        case (None, v) =>
          None -> v.map(nodeMapper.mapNodeObject)
      }
      Input(value)
    }
  }
}

/**
 * https://www.w3.org/TR/json-ld11/#language-indexing
 *
 * https://www.w3.org/TR/json-ld11/#dfn-language-map
 *
 * @param key
 */
final case class LanguageMapBinding(key: BindingKey) extends Binding {
  import LanguageMapBinding.Input

  def bind(input: Input): NodeEntry = {
    val entries: Seq[NodeEntry] = input.entries
    NodeEntry(key.name, NodeObject(entries: _*))
  }

  def ->(value: Input): NodeEntry = bind(value)
}

object LanguageMapBinding {
  final case class Input(input: Map[Option[String], Seq[String]]) {
    def entries: Seq[NodeEntry] = {
      input.map {
        case (Some(k), list) => NodeEntry(k, list.map(v => StringLiteral(v)): _*)
        case (None, list)    => NodeEntry(Keyword.`@none`.name, list.map(v => StringLiteral(v)): _*)
      }.toSeq
    }
  }

  object Input {
    implicit def stringInput(elements: Map[String, String]): Input = {
      val value = elements.map { case (k, v) =>
        Option(k) -> Seq(v)
      }
      Input(value)
    }

    implicit def stringSeqInput(elements: Map[String, Seq[String]]): Input = {
      val value = elements.map { case (k, v) =>
        Option(k) -> v
      }
      Input(value)
    }

    implicit def optionStringInput(elements: Map[Option[String], String]): Input = {
      val value = elements.map { case (k, v) =>
        k -> Seq(v)
      }
      Input(value)
    }

    // Oddly enough, this only works when you have both Some("foo") and None as elements,
    // if None is the only element it goes a bit odd.
    implicit def optionStringSeqInput(elements: Map[Option[String], Seq[String]]): Input =
      Input(elements)
  }
}
