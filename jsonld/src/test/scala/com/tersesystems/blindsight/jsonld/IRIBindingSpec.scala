package com.tersesystems.blindsight.jsonld

import java.net.URI
import java.util.UUID

class IRIBindingSpec extends BaseSpec {

  "IRI binding" should {
    "bind IRI from String" in {
      val id          = Keyword.`@id`.bindIRI
      val node = NodeObject(
        id -> IRI("http://www.wikidata.org/entity/Q76")
      )

      val nodeMap = toMap(node)
      nodeMap(id.key.name) should be(StringLiteral("http://www.wikidata.org/entity/Q76"))
    }

    "bind IRI from URI" in {
      val id       = Keyword.`@id`.bindIRI
      val uri: URI = new URI("http://www.wikidata.org/entity/Q76")
      val node = NodeObject(
        id -> IRI(uri)
      )

      val nodeMap = toMap(node)
      nodeMap(id.key.name) should be(StringLiteral("http://www.wikidata.org/entity/Q76"))
    }

    "bind IRI from URL" in {
      val id       = Keyword.`@id`.bindIRI
      val uri: URI = new URI("http://www.wikidata.org/entity/Q76")
      val node = NodeObject(
        id -> IRI(uri)
      )

      val nodeMap = toMap(node)
      nodeMap(id.key.name) should be(StringLiteral("http://www.wikidata.org/entity/Q76"))
    }

    "bind IRI from UUID" in {
      val uuid = UUID.randomUUID()
      val uuidIRI: IRI = IRI(uuid)
      val id       = Keyword.`@id`.bindIRI
      val node = NodeObject(
        id -> uuidIRI
      )

      val nodeMap = toMap(node)
      nodeMap(id.key.name) should be(StringLiteral("urn:uuid:" + uuid.toString))
    }

    "bind IRI from Person" in {
      val id             = Keyword.`@id`.bindIRI[Person]
      val person: Person = Person("http://www.wikidata.org/entity/Q76", name = "Barack Obama")
      val node = NodeObject(
        id -> person
      )

      val nodeMap = toMap(node)
      nodeMap(id.key.name) should be(StringLiteral("http://www.wikidata.org/entity/Q76"))
    }

    "bind multiple IRIs" in {
      // https://www.w3.org/TR/json-ld11/#specifying-the-type

      val schemaOrg: Vocab = IRI("https://schema.org/").vocab
      val foaf: Term       = IRI("http://xmlns.com/foaf/0.1/").term("foaf")
      val foafPerson       = foaf("Person")
      val schemaPerson     = schemaOrg("Person")

      val `@type` = Keyword.`@type`.bindIRIs
      val node = NodeObject(
        `@type` -> Seq(schemaPerson, foafPerson)
      )

      val nodeMap = node.value.map(entry => entry.key -> entry.value).toMap
      nodeMap(`@type`.key.name) should contain theSameElementsAs
        Seq(StringLiteral("Person"), StringLiteral("foaf:Person"))
    }
  }

  case class Person(id: String, name: String)

  object Person {
    implicit val personIRIMapper: IRIValueMapper[Person] = IRIValueMapper(p => IRI(p.id))
  }
}
