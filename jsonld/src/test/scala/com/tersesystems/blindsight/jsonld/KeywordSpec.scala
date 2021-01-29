package com.tersesystems.blindsight.jsonld

class KeywordSpec extends BaseSpec {
  "Keyword" should {
    "alias to have a different name but same type" in {
      val idAlias = Keyword.`@id`.alias("id")

      idAlias.name should be("id")
      idAlias shouldBe a[Keyword.Id]
    }

    "Contain the alias as a key in a binding" in {
      val id               = Keyword.`@id`.alias("id").bindIRI
      val entry: NodeEntry = id -> IRI("http://example.com/exampleId")
      entry.key should be("id")
    }
  }

  "@included" should {
    // https://www.w3.org/TR/json-ld11/#included-nodes
    // https://www.w3.org/TR/json-ld11/#included-blocks
    "allow an array of node objects" in {
      val included = Keyword.`@included`.bindObjects[NodeObject]

      val nodeOne = NodeObject(
        `@id`   -> IRI("one"),
        `@type` -> schemaOrg("Occupation")
      )
      val nodeTwo = NodeObject(
        `@id`   -> IRI("two"),
        `@type` -> schemaOrg("Occupation")
      )
      val entry = included.bind(Seq(nodeOne, nodeTwo))
      entry.value should contain theSameElementsInOrderAs Seq(
        NodeObject(NodeEntry("@id", Value("one")), NodeEntry("@type", Value("Occupation"))),
        NodeObject(NodeEntry("@id", Value("two")), NodeEntry("@type", Value("Occupation")))
      )
    }
  }
  // XXX Add binding cases for all keywords so we can test out the results
}
