package com.tersesystems.blindsight.jsonld

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait JSONLDContext {
  val `@type` = Keyword.`@type`.bindIRI
  val `@id`   = Keyword.`@id`.bindIRI

  val schemaOrg: Vocab        = IRI("https://schema.org/").vocab
  val xsd: Term               = IRI("http://www.w3.org/2001/XMLSchema#").term("xsd")
  val xsdDate: CompactIRI     = xsd("date")
  val xsdDateTime: CompactIRI = xsd("dateTime")
}

abstract class BaseSpec extends AnyWordSpec with Matchers with JSONLDContext {

  protected def toMap(no: NodeObject): Map[String, Node] = {
    no.value.map(entry => entry.key -> entry.value.head).toMap
  }

}
