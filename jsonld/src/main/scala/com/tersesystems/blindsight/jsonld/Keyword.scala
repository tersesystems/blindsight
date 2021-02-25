package com.tersesystems.blindsight.jsonld

/**
 * A <a href="https://www.w3.org/TR/json-ld11/#dfn-keyword">keyword</a> is a string that is specific to JSON-LD.
 *
 * Different keywords have different key bindings available.
 *
 * @see <a href="https://w3c.github.io/json-ld-syntax/#syntax-tokens-and-keywords"></a>
 * @param name the name of the keyword.
 */
sealed trait Keyword[KeywordType <: Keyword[_]] {
  def name: String

  /**
   * A keyword can be aliased to another name.
   *
   * {{{
   * val exampleId = Keyword.`@id`.alias("exampleId").bindIRI
   * // prints out "exampleId": "http://example.com/exampleId"
   * val entry: NodeEntry = exampleId -> "http://example.com/exampleId"
   * }}}
   *
   * @see <a href="https://www.w3.org/TR/json-ld11/#aliasing-keywords">Aliasing Keywords</a>
   *
   * @param name the alias name of the keyword.
   * @return the same type using a different alias.
   */
  def alias(name: String): KeywordType

  override def toString: String = name
}

/**
 * Contains all the keywords defined in <a href="https://www.w3.org/TR/json-ld11/#keywords">document</a>.
 */
object Keyword {

  /**
   * Used to set the base IRI against which to resolve those relative IRI references which are
   * otherwise interpreted relative to the document. This keyword is described in § 4.1.3 Base IRI.
   */
  trait Base extends Keyword[Base] with IRIBindingKey
  private final case class BaseImpl(name: String) extends Base {
    override def alias(name: String): Base = copy(name)
  }
  val `@base`: Base = BaseImpl("@base")

  /**
   * Used to set the default container type for a term.
   *
   * Valid values are `@id`, `@set`, `@graph`, `@index`, `@language`, `@list`,
   * an array containing both `@index` and `@set`, or `@language` and `@set`.
   */
  trait Container extends Keyword[Container] with ValueBindingKey
  private final case class ContainerImpl(name: String) extends Container {
    override def alias(name: String): Container = copy(name)
  }
  val `@container`: Container = ContainerImpl("@container")

  /**
   * Used to define the short-hand names that are used throughout a JSON-LD document.
   */
  trait Context
      extends Keyword[Context]
      with ValueBindingKey
      with NodeObjectBindingKey
      with ListBindingKey
  private final case class ContextImpl(name: String) extends Context {
    override def alias(name: String): Context = copy(name)
  }
  val `@context`: Context = ContextImpl("@context")

  /**
   */
  trait Direction extends Keyword[Direction] with ValueBindingKey
  private final case class DirectionImpl(name: String) extends Direction {
    override def alias(name: String): Direction = copy(name)
  }
  val `@direction`: Direction = DirectionImpl("@direction")

  /**
   * Used to express a graph.
   */
  // XXX should this bind to something?
  trait Graph extends Keyword[Graph]
  private final case class GraphImpl(name: String, aliased: Option[Graph] = None) extends Graph {
    override def alias(name: String): Graph = copy(name)
  }
  val `@graph`: Graph = GraphImpl("@graph")

  /**
   * Used to uniquely identify node objects that are being described in the document with IRIs or blank node identifiers.
   */
  trait Id extends Keyword[Id] with IRIBindingKey
  private final case class IdImpl(name: String, aliased: Option[Id] = None) extends Id {
    override def alias(name: String): Id = copy(name)
  }
  val `@id`: Id = IdImpl("@id")

  /**
   * Used in a context definition to load an external context within which the containing context definition is merged.
   */
  trait Import extends Keyword[Import] with IRIBindingKey
  private final case class ImportImpl(name: String) extends Import {
    override def alias(name: String): Import = copy(name)
  }
  val `@import`: Import = ImportImpl("@import")

  /**
   * Used in a top-level node object to define an included block, for including secondary
   * node objects within another node object.
   *
   * An included block is either a node object or an array of node objects.
   */
  trait Included extends Keyword[Included] with NodeObjectBindingKey
  private final case class IncludedImpl(name: String) extends Included {
    override def alias(name: String): Included = copy(name)
  }
  val `@included`: Included = IncludedImpl("@included")

  /**
   * Used to specify that a container is used to index information and that processing should continue deeper into a JSON data structure.
   */
  trait Index extends Keyword[Index] with ValueBindingKey
  private final case class IndexImpl(name: String) extends Index {
    override def alias(name: String): Index = copy(name)
  }
  val `@index`: Index = IndexImpl("@index")

  /**
   * Used as the @type value of a JSON literal.
   */
  trait Json extends Keyword[Json]
  private final case class JsonImpl(name: String) extends Json {
    override def alias(name: String): Json = copy(name)
  }
  val `@json`: Json = JsonImpl("@json")

  /**
   * Used to specify the language for a particular string value or the default language of a JSON-LD document.
   */
  trait Language extends Keyword[Language] with ValueBindingKey
  private final case class LanguageImpl(name: String) extends Language {
    override def alias(name: String): Language = copy(name)
  }
  val `@language`: Language = LanguageImpl("@language")

  /**
   * Used to express an ordered set of data.
   */
  // XXX should this bind to something?
  trait List extends Keyword[List]
  private final case class ListImpl(name: String) extends List {
    override def alias(name: String): List = copy(name)
  }
  val `@list`: List = ListImpl("@list")

  /**
   * Used to define a property of a node object that groups together properties of that node,
   * but is not an edge in the graph.
   *
   * @see <a href="https://www.w3.org/TR/json-ld11/#nested-properties">Nested Properties</a>.
   */
  // XXX Not 100% sure that @nest takes a value and not an IRI
  trait Nest extends Keyword[Nest] with ValueBindingKey
  private final case class NestImpl(name: String) extends Nest {
    override def alias(name: String): Nest = copy(name)
  }
  val `@nest`: Nest = NestImpl("@nest")

  /**
   * Used as an index value in an index map, id map, language map, type map, or elsewhere
   * where a map is used to index into other values, when the indexed node does not have the feature being indexed.
   */
  trait None extends Keyword[None] with IRIBindingKey
  private final case class NoneImpl(name: String) extends None {
    override def alias(name: String): None = copy(name)
  }
  val `@none`: None = NoneImpl("@none")

  /**
   * With the value true, allows this term to be used to construct a compact IRI when compacting.
   * With the value false prevents the term from being used to construct a compact IRI.
   */
  // value MUST be true or false
  trait Prefix extends Keyword[Prefix] with ValueBindingKey
  private final case class PrefixImpl(name: String) extends Prefix {
    override def alias(name: String): Prefix = copy(name)
  }
  val `@prefix`: Prefix = PrefixImpl("@prefix")

  /**
   * Used in a context definition to change the scope of that context.
   */
  // value MUST be true or false
  trait Propagate extends Keyword[Propagate] with ValueBindingKey
  private final case class PropagateImpl(name: String) extends Propagate {
    override def alias(name: String): Propagate = copy(name)
  }
  val `@propagate`: Propagate = PropagateImpl("@propagate")

  /**
   * Used to express reverse properties.
   */
  //case object `@reverse` extends Keyword("@reverse") with IRIBindingKey
  trait Reverse extends Keyword[Reverse] with IRIBindingKey
  private final case class ReverseImpl(name: String) extends Reverse {
    override def alias(name: String): Reverse = copy(name)
  }
  val `@reverse`: Reverse = ReverseImpl("@reverse")

  /**
   * Used to express an unordered set of data and to ensure that values are always represented as arrays.
   */
  // XXX A set can have a single value or a node object???
  trait Set extends Keyword[Set] with SetBindingKey with ValueBindingKey with NodeObjectBindingKey
  private final case class SetImpl(name: String) extends Set {
    override def alias(name: String): Set = copy(name)
  }
  val `@set`: Set = SetImpl("@set")

  /**
   * Used to set the type of a node or the datatype of a typed value.
   */
  trait Type extends Keyword[Type] with IRIBindingKey
  private final case class TypeImpl(name: String) extends Type {
    override def alias(name: String): Type = copy(name)
  }
  val `@type`: Type = TypeImpl("@type")

  /**
   * Used to specify the data that is associated with a particular property in the graph.
   */
  trait Value extends Keyword[Value] with ValueBindingKey
  private final case class ValueImpl(name: String) extends Value {
    override def alias(name: String): Value = copy(name)
  }
  val `@value`: Value = ValueImpl("@value")

  /**
   * Used in a context definition to set the processing mode.
   */
  //case object `@version` extends Keyword("@version") with ValueBindingKey
  trait Version extends Keyword[Version] with ValueBindingKey
  private final case class VersionImpl(name: String) extends Version {
    override def alias(name: String): Version = copy(name)
  }
  val `@version`: Version = VersionImpl("@version")

  /**
   * Used to expand properties and values in @type with a common prefix IRI.
   */
  trait Vocab extends Keyword[Vocab] with ValueBindingKey with IRIBindingKey
  private final case class VocabImpl(name: String) extends Vocab {
    override def alias(name: String): Vocab = copy(name)
  }
  val `@vocab`: Vocab = VocabImpl("@vocab")
}
