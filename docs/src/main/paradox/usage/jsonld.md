# JSON-LD

## Overview

[JSON-LD](https://www.w3.org/2018/json-ld-wg/) is a lightweight format that uses JSON to describe structured data at a
higher level. Information in JSON-LD can be used to link data through IRIs and represent lists, sets, and types. JSON-LD
provides [unambiguous meaning](http://www.seoskeptic.com/what-is-json-ld/) through typed values and node objects. One
especially nice feature of JSON-LD is that it can be easily imported into graph databases, as JSON-LD can be converted
into an [RDF representation](https://en.wikipedia.org/wiki/Resource_Description_Framework), a common data model for
knowledge management and reasoning.

Blindsight supports JSON-LD by binding Scala types to JSON-LD and providing type classes to map data to JSON-LD.

> NOTE: This guide does not cover how to set up a JSON-LD context definition or creating an ontology. It is assumed that context is passed out of band from the individual log entries. Please see
[JSON-LD Best Practices](https://json-ld.org/spec/latest/json-ld-api-best-practices/) for a guide on building JSON-LD schema.

## Quick Start

The simplest possible JSON-LD is as follows:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.jsonld._

val yourSchema = IRI("https://yourcompany.com/jsonld/schema#").vocab
val stringProperty = yourSchema("stringProperty").bindValue[String]
val nodeObject = NodeObject(stringProperty -> "stringValue")
```

The first line sets up an @scaladoc[IRI](com.tersesystems.blindsight.jsonld.IRI).
An [IRI](https://www.w3.org/TR/json-ld11/#iris) defines a unique prefix for the properties. The IRI does not have to
exist on the Internet, but it can be helpful, especially when using common schemas like [schema.org](https://schema.org)
. The IRI is then turned into a @scaladoc[Vocab](com.tersesystems.blindsight.jsonld.Vocab) instance using `vocab` --
this indicates that this is the [default vocabulary](https://www.w3.org/TR/json-ld11/#default-vocabulary) for the node
object.

The second line sets up a @scaladoc[Term](com.tersesystems.blindsight.jsonld.Term) that is then bound
using `bindValue[String]` returning a [ValueBinding](com.tersesystems.blindsight.jsonld.ValueBinding)
called `stringProperty`.

The third line defines a @scaladoc[NodeObject](com.tersesystems.blindsight.jsonld.NodeObject) and binds
the `stringProperty` to `stringValue`, creating a @scaladoc[NodeEntry](com.tersesystems.blindsight.jsonld.NodeEntry) and
passing it in as a property of the [node object](https://www.w3.org/TR/json-ld11/#dfn-node-object).

Finally, the fourth and fifth lines set up a @ref:[semantic logger](semantic.md) that logs only
@scaladoc[NodeObject](com.tersesystems.blindsight.jsonld.NodeObject), and logs the node object.

In practice, you will want to set up your IRIs and your bindings in one place so that only the last few lines are
necessary.

## Logging Node Objects

Once you have a node object, you will need to want to integrate it as a loggable statement. You can pass node objects
around as arguments using @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument):

```scala
implicit val nodeObjectToArgument: ToArgument[NodeObject] = ToArgument { nodeObject =>
  Argument(BlindsightASTMapping.toBObject(nodeObject))
}
logger.info("argument = {}", nodeObject)
```

Or as a marker using @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers):

```scala
implicit val nodeObjectToMarkers: ToMarkers[NodeObject] = ToMarkers { nodeObject =>
  Markers(BlindsightASTMapping.toBObject(nodeObject))
}

logger.info(Markers(nodeObject), "as a marker")
```

Or even as a statement @scaladoc[ToStatement](com.tersesystems.blindsight.ToStatement):

```scala
implicit val nodeObjectToStatement: ToStatement[NodeObject] = ToStatement { nodeObject =>
  val args = Arguments(Argument(bobj("@graph" -> BlindsightASTMapping.toBObject(nodeObject))))
  Statement(message = "{}", arguments = args)
}

val nodeObjectLogger = logger.semantic[NodeObject]
nodeObjectLogger.info(nodeObject)
```

## Establishing a Context

The easiest way to set up convenient logging in JSON-LD is to define terms and bindings in a trait.

```scala
trait YourContext {
  val yourSchema = IRI("https://yourcompany.com/jsonld/schema#").vocab
  val stringProperty = yourSchema("stringProperty").bindValue[String]
}

object YourContext extends YourContext

```

As you build up your context, you'll add more bindings to it. For example, you may want to define date and time
properties. In JSON-LD, the idiom is to use the [XMLSchema namespace](http://www.w3.org/2001/XMLSchema), so we can add
that in:

```scala
trait XSDContext {
  val xsd: Term = IRI("http://www.w3.org/2001/XMLSchema#").term("xsd")
  val xsdDate: CompactIRI = xsd("date") // "xsd:date"
  val xsdDateTime: CompactIRI = xsd("dateTime") // "xsd:dateTime"
}

trait YourContext extends XSDContext {
  ...
}
```

Note that here, we're using @scaladoc[Term](com.tersesystems.blindsight.jsonld.Term) to provide a
@scaladoc[CompactIRI](com.tersesystems.blindsight.jsonld.CompactIRI) for `xsdDate` and `xsdDateTime`.
A [compact IRI](https://www.w3.org/TR/json-ld11/#compact-iris) expresses an IRI using a prefix and suffix separated by a
colon as a shorthand.

Using the context, we can go ahead and fill out our JSON-LD mapping, starting with simple values and working up to more
advanced concepts.

## Defining Values

A "value" in JSON-LD is a [leaf node](https://www.w3.org/TR/json-ld11/#describing-values) that describes an atomic value
such as string, a number, a boolean, or a date. If JSON-LD can represent the value natively in JSON, it writes out the
corresponding literal, and if it doesn't (most notably for internationalized strings and dates), it uses a "value
object" which is a JSON object containing the needed metadata along with the value.

Broadly speaking, there are two types of values: literal values and typed values.

### Literal Values

A @scaladoc[LiteralValue](com.tersesystems.blindsight.jsonld.LiteralValue) has a direct representation in JSON. Strings,
boolean values, and numbers can all be represented, with `null` represented by `None`:

```scala
val name = schemaOrg("name").bindValue[String]
val abridged = schemaOrg("abridged").bindValue[Boolean]
val numberOfPages = schemaOrg("numberOfPages").bindValue[Int]
val subtitle = schemaOrg("subtitle").bindValue[Option[String]]

val bookType = schemaOrg("Book")

val abridgedMobyDick = NodeObject(
  `@type` -> bookType,
  name -> "Moby Dick",
  subtitle -> None,
  abridged -> true,
  numberOfPages -> 12
)
```

Note that here the `@type` of the node object is specified as a "Book" -- this is not required, but is useful to let
JSON-LD know that there are some known properties (`name`, `numberOfPages`) associated with the node object.

There is one complication, which is
that [string internationalization](https://www.w3.org/TR/json-ld11/#string-internationalization) may include string
direction and a language, represented with a @scaladoc[StringValue](com.tersesystems.blindsight.jsonld.StringValue).

```scala
val blurb = yourSchema("blurb").bindValue[StringValue]
val book = NodeObject(
  blurb -> Value("Some Blurb in English", StringDirection.LeftToRight, "en")
)
```

The expanded form of JSON-LD always renders an array of values. If you want to use the expanded form, you can
use `bindValues` rather than `bindValue`.

```scala
val names = yourSchema("names").bindValues[String]
val book = NodeObject(
  names -> Seq("Me", "You") 
)
```

### JSON Values

JSON-LD also allows for [JSON Literals](https://www.w3.org/TR/json-ld11/#json-literals). JSON Literals are handled using
Blindsight's @ref:[DSL](dsl.md):

```scala
import com.tersesystems.blindsight.DSL._

val jsonValue = yourSchema("jsonValue").bindValue[BObject]
val bobject = bobj("key" -> "value")
val nodeObject = NodeObject(jsonValue -> bobject)
```

or for an array:

```scala
import com.tersesystems.blindsight.DSL._

val jsonArray = schemaOrg("jsonArray").bindValue[BArray]
val barray = BArray(List(1, 2, 3))
val nodeObject = NodeObject(jsonArray -> barray)
```

### Typed Values

[Typed values](https://www.w3.org/TR/json-ld11/#typed-values) such as dates are represented with
@scaladoc[TypedValue](com.tersesystems.blindsight.jsonld.TypedValue).

```scala
val dateCreated = schemaOrg("dateCreated").bindValue[TypedValue]
val localDate = LocalDate.of(2020, 1, 1)
val abridgedMobyDick = NodeObject(
  `@type` -> bookType,
  name -> "Moby Dick",
  dateCreated -> Value(DateTimeFormatter.ISO_DATE.format(localDate), xsdDate)
)
```

Because all typed values look the same to the Scala compiler, it's better to use a custom value mapper.

### Custom Value Mapping

A custom @scaladoc[ValueMapper](com.tersesystems.blindsight.jsonld.ValueMapper) converts from a Scala type to a value
using a type class instance. This is particularly useful for dates and times.

```scala
implicit val localDateMapper: ValueMapper[LocalDate] = ValueMapper { date =>
  Value(DateTimeFormatter.ISO_DATE.format(date), xsdDate)
}

val dateCreated = yourSchema("dateCreated").bindValue[LocalDate]
val abridgedMobyDick = NodeObject(
  `@type` -> "Book",
  name -> "Moby Dick",
  dateCreated -> LocalDate.of(2020, 1, 1)
)
```

or currencies:

```scala
implicit val currencyMapper: ValueMapper[Currency] = ValueMapper { currency =>
  Value(currency.getCurrencyCode)
}
val currency: ValueBinding[Currency] = schemaOrg("currency").bindValue[Currency]
```

## Node Objects

A @scaladoc[NodeObject](com.tersesystems.blindsight.jsonld.NodeObject) can also contain other node objects. This is
referred to as [object embedding](https://www.w3.org/TR/json-ld11/#embedding).

Binding to a node object is done using `bindObject`, or you can bind an `Iterable` using `bindObjects`.

```scala
val occupationType = schemaOrg("Occupation")
val monetaryAmountType = schemaOrg("MonetaryAmount")
val estimatedSalary = schemaOrg("estimatedSalary").bindObject[NodeObject]

val occupation = NodeObject(
  `@type` -> occupationType,
  name -> "Code Monkey",
  estimatedSalary -> NodeObject(
    `@type` -> monetaryAmountType,
    currency -> Currency.getInstance("USD"),
    value -> 1
  )
)
```

### Node Object Properties Ordering

As a useful guide for streaming JSON-LD, you
should [order properties](https://w3c.github.io/json-ld-streaming/#key-ordering-recommended) in the following priority:

* `@context` should come first, if defined.
* `@type`: comes after `@context`, if defined.
* `@id`: comes after `@type` or context if defined.

### Custom NodeObject Mapping

The easiest way to define a node object mapping is to define a case class and a type class instance of a
@scaladoc[NodeObjectMapper](com.tersesystems.blindsight.jsonld.NodeObjectMapper).

```scala
case class MonetaryAmount(currency: Currency, value: Int)

object MonetaryAmount {
  implicit val monetaryAmountMapper: NodeObjectMapper[MonetaryAmount] = NodeObjectMapper { ma =>
    NodeObject(
      `@type` -> monetaryAmountType,
      currency -> ma.currency,
      value -> ma.value
    )
  }
}

```

And then you can bind to the `MonetaryAmount` directly:

```scala
val occupationType = schemaOrg("Occupation")
val monetaryAmountType = schemaOrg("MonetaryAmount")
val estimatedSalary = schemaOrg("estimatedSalary").bindObject[MonetaryAmount]

val occupation = NodeObject(
  `@type` -> occupationType,
  name -> "Code Monkey",
  estimatedSalary -> MonetaryAmount(USD, 1)
)
```

## IRIs

IRIs are the foundation of linked data, and JSON-LD has several ways of representing a value that expands to a full IRI.  [IRI values](https://www.w3.org/TR/json-ld11/#iris) can show as compact IRIs, relative IRI references, or full IRIs.

In Blindsight, the root trait is @scaladoc[IRIValue](com.tersesystems.blindsight.jsonld.IRIValue), which can expand out
to a number of implementations.

#### IRI, PropertyIRI

The @scaladoc[IRI](com.tersesystems.blindsight.jsonld.IRI) in Blindsight refers to a full IRI.  An IRI can be created from a `java.net.URL`, a `java.net.URI`, or a `java.util.UUID` instance.  Blindsight only knows about the string representation, and does not keep any extra URL or URI information.

```scala
val textIRI = IRI("https://schema.org/")
val uuidIRI: IRI = IRI(UUID.randomUUID())
val uriIRI: IRI = IRI(new java.net.URI("https://schema.org"))
```

A @scaladoc[PropertyIRI](com.tersesystems.blindsight.jsonld.PropertyIRI) is created from an @scaladoc[IRI](com.tersesystems.blindsight.jsonld.IRI) and returns the full IRI plus property.

```scala
val niemCore = IRI("http://release.niem.gov/niem/niem-core/4.0/#")

// value prints "http://release.niem.gov/niem/niem-core/4.0/#PersonGivenName"
val personGivenName = niemCore.property("PersonGivenName")
```

Because full IRIs can be unwieldy in a document, JSON-LD has ways of compacting IRIs, by representing an IRI prefix as either a term or a default vocabulary in compact IRIs.

A [term](https://www.w3.org/TR/json-ld11/#terms) is a prefix that is used as a "label" for an IRI.  Blindsight creates a @scaladoc[Term](com.tersesystems.blindsight.jsonld.Term) using `iri.term("prefix")`:

```scala
val xmlSchema = IRI("http://www.w3.org/2001/XMLSchema#")
val xsd: Term = xmlSchema.term("xsd")
```

A [compact IRI](https://www.w3.org/TR/json-ld11/#dfn-compact-iri) can be created from a term and property.  In Blindsight, this is done using `term.apply("propertyName")` and creates a @scaladoc[CompactIRI](com.tersesystems.blindsight.jsonld.CompactIRI):

```scala
val xsdDateTime: CompactIRI = xsd("dateTime") // "xsd:dateTime"

// Prints out the current instant as a typed value with xsd:dateTime
val dateValue = Value(Instant.now.toString, xsdDateTime)
```

A [default vocabulary](https://www.w3.org/TR/json-ld11/#default-vocabulary) is used when a property name is presented without a prefix.  In Blindsight, this is done using `iri.vocab`, and returns a @scaladoc[Vocab](com.tersesystems.blindsight.jsonld.Vocab):

```scala
val schemaVocab = IRI("https://schema.org/").vocab
val schemaPerson = schemaVocab("Person") // prints "Person"
```

There are also points where a [relative IRI reference](https://www.w3.org/TR/json-ld11/#iris) in the form "some/path/fragment" is defined.  In JSON-LD, relative IRIs are relative to the [base IRI](https://www.w3.org/TR/json-ld11/#base-iri).  A base IRI is created from an IRI using `base`, returning a @scaladoc[Base](com.tersesystems.blindsight.jsonld.Base), which can then return a [RelativeIRI](com.tersesystems.blindsight.jsonld.RelativeIRI)

```scala
val baseIRI: Base = IRI("http://example.com/").base
val enPost: RelativeIRI = baseIRI("1/en") // returns "1/en"
```

Relative IRIs are often used in [ID Maps](https://w3c.github.io/json-ld-syntax/#node-identifier-indexing).

### Binding IRI

Binding an IRI is done through `bindIRI`:

```scala
val id          = Keyword.`@id`.bindIRI
val node = NodeObject(
  id -> IRI("http://www.wikidata.org/entity/Q76")
)
```

You can bind to an array of IRIs using `bindIRIs`.  For example, you may want to specify multiple types to indicate that a node object has properties for both the "foaf" concept of a person and the "schema" concept of a person.

```scala
val schemaOrg: Vocab = IRI("https://schema.org/").vocab
val foaf: Term = IRI("http://xmlns.com/foaf/0.1/").term("foaf")
val foafPerson = foaf("Person")
val schemaPerson = schemaOrg("Person")

val `@type` = Keyword.`@type`.bindIRIs
val node = NodeObject(
  `@type` -> Seq(schemaPerson, foafPerson)
)
```

### Custom IRI Mapper

Creating a @scaladoc[CustomIRIMapper](com.tersesystems.blindsight.jsonld.CustomIRIMapper) is relatively simple if you
have a unique ID field that can be exposed as an IRI:

```scala
case class Person(id: String, name: String)

object Person {
  implicit val personIRIMapper: IRIValueMapper[Person] = IRIValueMapper(p => IRI(p.id))
}
```

Once you have the custom IRI mapper for `[Person]`, you can use `bindIRI[Person]` and then it will only bind to
instances of `Person`:

```sibling
val sibling = schemaOrg("sibling").bindIRI[Person]
val aumaObama: Person = Person("https://www.wikidata.org/wiki/Q773197", name = "Auma Obama")
val barackObama = NodeObject(
  `@id` -> IRI("https://www.wikidata.org/wiki/Q76"),
  `@type` -> personType,
  sibling -> aumaObama
)
```

## List Objects

[Lists](https://www.w3.org/TR/json-ld11/#lists) in JSON-LD indicate an ordered set of elements.  In the expanded form of JSON-LD they are represented as list objects, but may be rendered as JSON arrays when compacted.

Binding to a list is done using `bindList[T]`, where `T` is the element type, returning an instance of  @scaladoc[ListObject](com.tersesystems.blindsight.jsonld.ListObject).  You can bind any `Iterable[T]`, but `Seq` is most common.  For example, to define a list of strings you would do the following:

```scala
val listOfStrings = schemaOrg("listOfStrings").bindList[String]

val aListOfStrings = NodeObject(
  listOfStrings -> Seq("One", "Two", "Three", "Four")
)
```

If you want to represent a null element, then you should use `Option[T]`, and `None` will represent `null`:

```scala
val optionalStrings = schemaOrg("optionalStrings").bindList[Option[String]]

val obj = NodeObject(
  optionalStrings -> Seq(Some("some"), None)
)
```

If you have elements that do not have a common type, you can also bind to `NodeObject` or `Node`:

```scala
val listOfNodes = schemaOrg("listOfNodes").bindList[NodeObject]

val listObject = NodeObject(
  listOfNodes -> Seq(
    NodeObject(name -> "firstNode"),
    NodeObject(name -> "secondNode")
  )
)
```

#### Custom ListObject Binding

Binding complex elements with nested lists can be done with custom mapping.  For example, to render [the co-ordinates example]( https://w3c.github.io/json-ld-syntax/#example-83-coordinates-expressed-in-geojson), you can do the following:

```scala
trait MyGeoContext {
  val vocab = IRI("https://purl.org/geojson/vocab#")
  val bbox     = geoJson("bbox").bindList[Double]
  val geometry = geoJson("geometry").bindObject[Geometry]

  implicit def seqMapper: NodeMapper[Seq[Double]] =
    NodeMapper { iter =>
      val mapper = implicitly[NodeMapper[Double]]
      ListObject(iter.map(mapper.mapNode))
    }

  val coordinates = geoJson("coordinates").bindList[Seq[Double]]
}

final case class Geometry(`@type`: String, coords: Seq[Seq[Double]])

object Geometry extends MyGeoContext {
  implicit val nodeMapper: NodeObjectMapper[Geometry] = NodeObjectMapper { geo =>
    val `@type` = Keyword.`@type`.bindIRI
    NodeObject(
      `@type` -> geo.`@type`,
      coordinates -> geo.coords
    )
  }
}
```

## Set Objects

[Sets](https://www.w3.org/TR/json-ld11/#sets) in JSON-LD represent unordered set of elements.  There is no uniqueness constraint as in Java and Scala sets.  

Use `bindSet[T]` where `T` is the element type.  The binding will return an instance of @scaladoc[SetObject](com.tersesystems.blindsight.jsonld.SetObject) to the node object.

For example, to bind a set of nicknames, you can do the following:

```scala
val foaf = IRI("http://xmlns.com/foaf/0.1/").term("foaf")

// A set of string values
val nick: SetBinding[String] = foaf("nick").bindSet[String]

// https://www.songfacts.com/lyrics/sheryl-crow/all-i-wanna-do
val personWithNicknames = NodeObject(
  nick -> Set("Bill", "Billy", "Mac", "Buddy")
)
```

To create a set that can contain `null`, call `bindSet` with an `Option[T]`.  Like the list binding, if there is no common type you can use `Node`, `Value` or `NodeObject`. 

```scala
val nodeSet = exampleOrg("nodeSet").bindSet[Option[Node]]
val nodeSetNode = NodeObject(
  nodeSet -> Set(
    Some(Value(1)),
    Some(nodeObject),
    None
  )
)
```

## Indexed Values

JSON-LD contains an [indexing mechanism](https://www.w3.org/TR/json-ld11/#indexed-values) that associates specific
indices with associated values. From a Scala perspective, these look like instances of
`Map[Key, Value]`, where `Key` and `Value` are dependent on the kind of indexing.

## Index Maps

In [data indexing](https://www.w3.org/TR/json-ld11/#data-indexing), the `Key` is a string that represents a key. The
result is an [index map](https://www.w3.org/TR/json-ld11/#index-maps).

There is no semantic meaning associated with the key, so only the type of the value is needed. You can create an index
map using `bindIndexMap`.

```scala
val athletes = schemaOrg("athletes").bindIndexMap
val name = schemaOrg("name").bindValue[String]
val position = schemaOrg("position").bindValue[String]
val person = schemaOrg("Person")
val sportsTeam = schemaOrg("SportsTeam")

val indexMapNode = NodeObject(
  name -> "San Francisco Giants",
  `@type` -> sportsTeam,
  athletes -> Map(
    "catcher" -> NodeObject(
      `@type` -> person,
      name -> "Buster Posey",
      position -> "Catcher"
    ),
    "pitcher" -> NodeObject(
      `@type` -> person,
      name -> "Madison Bumgarner",
      position -> "Starting Pitcher"
    )
  )
)
```

You can specify a `@none` key by using `None` as the index:

```scala
val optIndexMap = schemaOrg("optionalIndexMap").bindIndexMap

val indexMapNode = NodeObject(
  optIndexMap -> Map(
    Some("existing") -> "existingValue",
    None -> "defaultValue"
  )
)
```

You can specify a `null` value by using using `None` as the value:

```scala
val optIndexMap = schemaOrg("optionalIndexValueMap").bindIndexMap

val indexMapNode = NodeObject(
  optIndexMap -> Map(
    "exists" -> Option("existingValue"),
    "does not exist" -> None
  )
)
```

#### Property Based Index Map

If [property based index maps](https://www.w3.org/TR/json-ld11/#property-based-index-maps) are used for
[indexing](https://www.w3.org/TR/json-ld11/#property-based-data-indexing), then there is still no change as the ID is
still a string.

However, only mappers that resolve to a @scaladoc[NodeObject](com.tersesystems.blindsight.jsonld.NodeObject) are valid.
For example, with a context definition of `"@index": "schema:jobTitle"` the key would be:

```scala
val indexMapNode = NodeObject(
  name -> "San Francisco Giants",
  `@type` -> sportsTeam,
  athletes -> Map(
    "Catcher" -> NodeObject(
      `@type` -> person,
      name -> "Buster Posey"
    ),
    "Starting Pitcher" -> NodeObject(
      `@type` -> person,
      name -> "Madison Bumgarner"
    )
  )
)
```

### ID Maps

In [node id indexing](https://www.w3.org/TR/json-ld11/#node-identifier-indexing), the `Key` is an IRI value, and
the `Value` is @scaladoc[NodeObject](com.tersesystems.blindsight.jsonld.NodeObject)

You can create an [ID Map](https://www.w3.org/TR/json-ld11/#id-maps) using `bindIdMap` with
@scaladoc[IRIValueMapper](com.tersesystems.blindsight.jsonld.IRIValueMapper) and
@scaladoc[NodeObjectMapper](com.tersesystems.blindsight.jsonld.NodeObjectMapper).

For example, to create an ID map that binds `RelativeIRI` to `NodeObject` you would use:

```scala
val post = schemaOrg("post").bindIdMap
val exampleCom = IRI("http://example.com/")
val baseExampleCom = exampleCom.base
val node = NodeObject(
  `@id` -> exampleCom,
  `@type` -> schemaOrg("Blog"),
  name -> "World Financial News",
  post -> Map(
    baseExampleCom("1/en") -> NodeObject(
      body -> "World commodities were up today with heavy trading of crude oil...",
      words -> 1539
    ),
    baseExampleCom("1/de") -> NodeObject(
      body -> "Die Werte an Warenbörsen stiegen im Sog eines starken Handels von Rohöl...",
      words -> 1204
    )
  )
)
```

### Language Maps

In [language indexing](https://www.w3.org/TR/json-ld11/#language-indexing), the key is a string representing
the [BCP47 language tag](https://en.wikipedia.org/wiki/IETF_language_tag), and the value is a string or array of
strings.

To create a [language map](https://www.w3.org/TR/json-ld11/#language-maps), use `bindLanguageMap`.

The simplest matching is through a direct map:

```scala
val languageMap: LanguageMapBinding =
  schemaOrg("languageMap").bindLanguageMap

val nodeObject: NodeObject = NodeObject(
  languageMap -> Map(
    "en" -> "English",
    "fr" -> "Français",
    "de" -> "Deutsch",
    "ar" -> "موبي ديك"
  )
)
```

but the binding can also take an array of strings:

```scala
val nodeObject: NodeObject = NodeObject(
  languageMap -> Map(
    "en" -> Seq("English", "Still English")
  )
)
```

and if `@none` is needed then use `Some` and `None` as keys:

```scala

val nodeObject: NodeObject = NodeObject(
  languageMap -> Map(
    Some("en") -> "English",
    None -> "derp"
  )
)

```

### Type Maps

In [Node Type Indexing](https://www.w3.org/TR/json-ld11/#node-type-indexing), the key is an IRI value representing
the `@type` of the associated node object, and the value must be a node object or an array of node objects.

To create a [type map](https://www.w3.org/TR/json-ld11/#type-maps), use `bindTypeMap`.

```scala
val affiliation = schemaOrg("affiliation").bindTypeMap
val node = NodeObject(
  affiliation -> Map(
    schemaTerm("Corporation") -> NodeObject(
      `@id` -> IRI("https://digitalbazaar.com/"),
      name -> "Digital Bazaar"
    ),
    schemaTerm("ProfessionalService") -> NodeObject(
      `@id` -> IRI("https://spec-ops.io"),
      name -> "Spec-Ops"
    )
  )
)
```

To bind an array, use an `Iterable`:

```scala
val node = NodeObject(
  affiliation -> Map(
    schemaTerm("Corporation") -> Seq(
      NodeObject(
        `@id` -> IRI("https://corp1.com/"),
        name -> "Corporation One"
      ),
      NodeObject(
        `@id` -> IRI("https://corp2.io"),
        name -> "Corporation 2"
      )
    )
  )
)
```
