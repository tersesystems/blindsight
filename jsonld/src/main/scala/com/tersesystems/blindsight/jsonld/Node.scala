package com.tersesystems.blindsight.jsonld

import com.tersesystems.blindsight.AST._

import java.net.{URI, URL}
import java.util.UUID

/**
 * A Node is the base type for all elements, and is used for parsing.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#uses-of-json-objects">Uses of JSON objects</a>
 */
sealed trait Node

object Node {

  /**
   * A factory method to create a [[Node]] using a [[NodeMapper]].
   */
  def apply[T: NodeMapper](nodeValue: T): Node = implicitly[NodeMapper[T]].mapNode(nodeValue)

  // A Node can always be mapped back to itself.  This is useful when we have bindings to
  // a Node explicitly.
  implicit val nodeMapper: NodeMapper[Node] = NodeMapper(identity)
}

/**
 * A value is an approximation for a JSON-LD value, which can be a native JSON literal
 * (string, number, boolean) or a JSON object (value object, typed value).  In the case
 * of strings with a direction or a language, the string value may be a value object
 * rather than a JSON literal.
 *
 * This is a sealed abstract class because JSON-LD has a fixed number of values.
 *
 * You will normally use a value constructor rather than the specific instances, i.e.
 *
 * {{{
 * val stringValue: Value[String] = Value("some string")
 * val numValue: Value[Int] = Value(1)
 * }}}
 *
 * @tparam T the underlying scala type.
 */
sealed abstract class Value[T] extends Node {
  def value: T

  // override def toString: String = value.toString
}

object Value {

  /**
   * Factory for value mappers.
   *
   * {{{
   * implicit val fooMapper: ValueMapper[Foo] = ...
   * val foo: Foo = ...
   * val fooValue: Value[_] = Value(foo)
   * }}}
   *
   * @param v the value
   * @tparam T the type
   * @return the value instance
   */
  def apply[T: ValueMapper](v: T): Value[_] = {
    val mapper = implicitly[ValueMapper[T]]
    mapper.mapValue(v)
  }

  /**
   * A convenience constructor for a string value with a string direction.
   */
  def apply(v: String, stringDirection: StringDirection): StringValue = {
    StringValue(v, direction = Some(stringDirection), lang = None)
  }

  /**
   * A convenience constructor for a string value with a direction and locale.
   */
  def apply(v: String, stringDirection: StringDirection, lang: String): StringValue = {
    StringValue(v, direction = Some(stringDirection), Some(lang))
  }

  /**
   * A convenience constructor for a JSON-LD typed value.
   *
   * @param value the string value.
   * @param valueType the IRI value used.
   * @return a typed value.
   */
  def apply(value: String, valueType: IRIValue): TypedValue =
    TypedValue(value, valueType)

  /**
   * Convenience function for returning [[NullLiteral]], a value representing `null`.
   *
   * @return a value indicating `null`
   */
  def none: Value[None.type] = NullLiteral
}

/**
 * A value that represents a literal type in JSON.
 *
 * @tparam T the underlying scala type.
 */
sealed trait LiteralValue[T] extends Value[T]

/**
 * Represents a JSON string literal.
 *
 * @param value the string value.
 */
final case class StringLiteral(value: String) extends LiteralValue[String]

/**
 * Represents a JSON number literal.
 *
 * @param value the number value.
 */
final case class NumberLiteral(value: Number) extends LiteralValue[Number]

/**
 * Represents a JSON boolean literal.
 *
 * @param value the boolean value.
 */
sealed abstract class BooleanLiteral(val value: Boolean) extends LiteralValue[Boolean]

object BooleanLiteral {
  def apply(value: Boolean): BooleanLiteral = if (value) True else False

  case object True extends BooleanLiteral(true)

  case object False extends BooleanLiteral(false)
}

/**
 * A JSON literal value that returns a BObject.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#json-literals">JSON Literals</a>
 *
 * @param value the object representing JSON.
 */
final case class JsonObjectLiteral(value: BObject) extends LiteralValue[BObject]

/**
 * A JSON literal value that returns a BArray.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#json-literals">JSON Literals</a>
 *
 * @param value the array representing JSON literal objects.
 */
final case class JsonArrayLiteral(value: BArray) extends LiteralValue[BArray]

/**
 * The literal representing `null` in a value.
 *
 * This is mapped to `None` so that it works more intuitively with optional values.
 */
case object NullLiteral extends LiteralValue[None.type] {
  override val value: None.type = None
}

/**
 * An IRI value is any resource that expands out to an IRI, such as a term, a compact IRI.
 *
 * @see <a href="https://www.w3.org/TR/json-ld/#iris">IRIs</a>
 */
sealed trait IRIValue
    extends Node
    with ValueBindingKey
    with IRIBindingKey
    with NodeObjectBindingKey
    with ListBindingKey
    with IndexMapBindingKey
    with TypeMapBindingKey
    with LanguageMapBindingKey
    with IdMapBindingKey
    with SetBindingKey {
  def value: String
}

/**
 * This is a full IRI.
 *
 * @param iri the full IRI string
 */
final case class IRI private[blindsight] (value: String) extends IRIValue {

  /**
   * Creates a [[Vocab]] from the IRI.
   *
   * @return a vocab
   */
  def vocab: Vocab = Vocab(this)

  /**
   * Creates a relative path from this IRI, which is considered the base IRI.
   *
   * This is most often used in
   * <a href="https://w3c.github.io/json-ld-syntax/#node-identifier-indexing">Node Identifier Indexing</a>
   *
   * {{{
   * // "@base": "http://example.com/posts/",
   * val base = IRI("http://example.com/document.jsonld").base
   * }}}
   *
   * @param path
   * @return
   */
  def base: Base = Base(this)

  /**
   * Creates a term.  This creates a key that contains the prefix associated with the term.
   *
   * {{{
   * val xsd: Term               = IRI("http://www.w3.org/2001/XMLSchema#").term("xsd")
   * val xsdDate: CompactIRI     = xsd("date") // prints "xsd:date"
   * val xsdDateTime: CompactIRI = xsd("dateTime") // prints "xsd:dateTime"
   * }}}
   *
   * @param name
   * @return
   */
  def term(termName: String): Term = Term(termName, this)

  /**
   * Creates a property IRI from the full IRI.  Unlike term, a property
   * is a concatenation of the full IRI, rather than producing a minimal
   * IRI.
   *
   * @param propertyName the property name
   * @return the property IRI
   */
  def property(propertyName: String): PropertyIRI = PropertyIRI(propertyName, this)

  /**
   *  @return
   */
  def name: String = value
}

object IRI {
  val UUIDNamespace = "urn:uuid:"

  def apply(url: URL): IRI = {
    apply(url.toURI)
  }

  def apply(uri: URI): IRI = {
    IRI(uri.toString)
  }

  // https://tools.ietf.org/html/rfc4122 has the namespace
  def apply(value: UUID): IRI = IRI(UUIDNamespace + value.toString)

  def apply[T: IRIValueMapper](t: T): IRI = {
    val mapper = implicitly[IRIValueMapper[T]]
    IRI(mapper.mapIRIValue(t).value)
  }
}

/**
 * A base URI renders as `@base` in a context.
 *
 * @param iri
 */
final case class Base private[blindsight] (iri: IRI) {

  /**
   * Creates a relative path from this IRI, which is considered the base IRI.
   *
   * This is most often used in
   * <a href="https://w3c.github.io/json-ld-syntax/#node-identifier-indexing">Node Identifier Indexing</a>
   *
   * {{{
   * // "@base": "http://example.com/posts/",
   * val base: Base = IRI("http://example.com/document.jsonld").base
   * val enPost: RelativeIRI = base("1/en")
   * }}}
   *
   * @param path the IRI path
   * @return the relative IRI
   */
  def apply(path: String): RelativeIRI = RelativeIRI(path, this)
}

final case class RelativeIRI(name: String, base: Base) extends IRIValue {
  def value: String = name
}

// A compact IRI has the form of prefix:suffix and is used as a way of expressing an IRI
// without needing to define separate term definitions for each IRI contained within a common
// vocabulary identified by prefix.
final case class CompactIRI private[blindsight] (term: Term, suffix: String) extends IRIValue {
  def name = s"${term.name}:$suffix"

  def value: String = name
}

/**
 * A Property IRI.  Unlike term, a property is a concatenation of the full IRI,
 * rather than producing a minimal IRI.
 *
 * {{{
 * val niemCore = IRI("http://release.niem.gov/niem/niem-core/4.0/#")
 *
 * // prints "http://release.niem.gov/niem/niem-core/4.0/#PersonGivenName"
 * val personGivenName = niemCore.property("PersonGivenName")
 * }}}
 */
final case class PropertyIRI(name: String, base: IRI) extends IRIValue {
  val value: String = base.value + name
}

/**
 * Creates keys that do not contain any prefix.
 *
 * {{{
 * val niemCore = IRI("http://release.niem.gov/niem/niem-core/4.0/#")
 *
 * // "@vocab": "http://release.niem.gov/niem/niem-core/4.0/#"
 * val ncVocab = niemCore.vocab
 *
 * // "PersonGivenName" without prefix
 * val personGivenName = ncVocab("PersonGivenName")
 * }}}
 */
final case class Vocab private[blindsight] (iri: IRI) {

  def name: String = Keyword.`@vocab`.name

  def value: String = iri.value

  def apply(propName: String): VocabProperty = VocabProperty(propName, this)
}

/**
 * A vocab property is a property that was created from the default vocabulary.
 *
 * @param value the property name
 * @param vocab the default vocabulary
 */
final case class VocabProperty(value: String, vocab: Vocab) extends IRIValue {
  def name: String = value
}

/**
 * A term is a short-hand string that expands to an IRI, blank node identifier, or keyword.
 *
 * https://www.w3.org/TR/json-ld11/#terms
 *
 * @param name
 * @param iri
 */
final case class Term private[blindsight] (name: String, iri: IRIValue) {
  assert(name.nonEmpty, "Empty string used for term!")
  assert(!name.startsWith("@"), "Term starts with at character @")
  assert(!name.contains(":"), "Term contains colon character :")

  def apply(propName: String): CompactIRI = CompactIRI(this, propName)

  def value: String = iri.value
}

/**
 * A typed value is a value associated with a IRI indicating the type's value.
 *
 * This is most often used for representing objects that do not have a single
 * JSON representation, such as dates.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#typed-values">Typed Values</a>
 *
 * @param value the string representation of the value
 * @param valueType the IRI type of the value
 */
final case class TypedValue(value: String, valueType: IRIValue) extends Value[String]

/**
 * The string direction associated with the string.
 *
 * @param value either "rtl" or "ltr", `null` is not supported
 */
sealed abstract class StringDirection(val value: String)

object StringDirection {
  case object RightToLeft extends StringDirection("rtl")
  case object LeftToRight extends StringDirection("ltr")
}

/**
 * A string value that is a value object that can contain a `@direction` and a `@lang`.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#value-objects">Value Objects</a>
 *
 * @param value the string value
 * @param direction the direction
 * @param lang the locale, only `getLanguage` is used.
 */
final case class StringValue(
    value: String,
    direction: Option[StringDirection] = None,
    lang: Option[String] = None
) extends Value[String] {
  override def toString: String = s"StringValue($value, $direction, $lang)"
}

/**
 * A node entry is used in the context of a [[NodeObject]] to indicate
 * an [entry](https://infra.spec.whatwg.org/#map-entry).
 *
 * Note that although in most cases a entry key is an IRI value, in
 * the case of an index map, the key is just a string.
 *
 * @param key the key of the node entry
 * @param value the value of the node entry.
 */
// XXX There should be a way to specify an NodeEntry that takes an array of values though.
final case class NodeEntry(key: String, value: Node*) {
  override def toString: String = s"NodeEntry(key=$key, value=${value.toSeq})"
}

object NodeEntry {
  // We can say a node entry by itself can be treated as a node object.
  implicit val nodeEntryMapper: NodeObjectMapper[NodeEntry] = NodeObjectMapper(NodeObject(_))
}

/**
 * A node object contains node entries, represented as a Seq of [[NodeEntry]].
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#node-objects">Node Objects</a>
 *
 * @param value a sequence of node entries.
 */
final case class NodeObject(value: NodeEntry*) extends Node {
  override def toString: String = {
    s"NodeObject(${value.map(_.toString).mkString(",")})"
  }
}

object NodeObject {

  /** A node object that represents no node object at all. */
  val Null: NodeObject = NodeObject()
}

/**
 * A list object.  This is an ordered set of nodes.
 *
 * Note that a list is heterogeneous, so it may contain both node objects and json literals.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#lists-and-sets">Lists</a>
 *
 * @param value an iterable of [[Node]]
 */
final case class ListObject(value: scala.collection.Iterable[Node]) extends Node {
  override def toString: String = {
    s"ListObject(${value.map(_.toString).mkString(",")})"
  }
}

object ListObject {
  // A list object is a node object that only contains a @list keyword.
  implicit val listObjectMapper: NodeObjectMapper[ListObject] = NodeObjectMapper { v =>
    NodeObject(NodeEntry(Keyword.`@list`.name, v))
  }
}

/**
 * A set represents an unordered set of values.
 *
 * A set object does not represent a mathematical set, and so does not
 * represent a scala `Set`.  Instead, it's more like a "collection" of nodes.
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/#dfn-set-object">Lists and Sets</a>
 *
 * @param value an iteration of nodes.
 */
final case class SetObject(value: scala.collection.Iterable[Node]) extends Node {
  override def toString: String = s"{${value.map(_.toString).mkString(",")}}"
}

object SetObject {
  // A set object is a node object that only contains a @set keyword.
  implicit val setObjectMapper: NodeObjectMapper[SetObject] = NodeObjectMapper { v =>
    NodeObject(NodeEntry(Keyword.`@set`.name, v))
  }
}
