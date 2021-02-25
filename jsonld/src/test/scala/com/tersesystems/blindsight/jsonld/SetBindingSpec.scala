package com.tersesystems.blindsight.jsonld

class SetBindingSpec extends BaseSpec {

  "A set" when {
    "defined" should {
      "convert to a set object" in {
        val foaf = IRI("http://xmlns.com/foaf/0.1/").term("foaf")

        val givenName = schemaOrg("givenName").bindValue[String]

        // A set of string values
        val nick: SetBinding[String] = foaf("nick").bindSet[String]

        // https://www.songfacts.com/lyrics/sheryl-crow/all-i-wanna-do
        val personWithNicknames = NodeObject(
          givenName -> "William",
          nick bind Set("Bill", "Billy", "Mac", "Buddy")
        )

        // And it should map directly to strings
        val nodeMap: Map[String, Node] = toMap(personWithNicknames)
        nodeMap(nick.key.name) should be(
          SetObject(
            Set(Value("Bill"), Value("Billy"), Value("Mac"), Value("Buddy"))
          )
        )
      }
    }

    "defined with hetrogenous typed values" in {
      // Real examples of heterogeneous list and set data is a bit unusual, but
      // dates are a good example where something may have the same meaning
      // in several different representations...
      val exampleOrg = IRI("https://example.org/").vocab

      // A set of typed values
      val dateSet = exampleOrg("dateSet").bindSet[TypedValue]

      import java.time.format.DateTimeFormatter
      import java.time.{Instant, LocalDate}

      // If we're not using shapeless/generic hsets and don't have
      // type class derivation here, then we have to use implicit conversion :-<
      implicit def localDate2Value(localDate: LocalDate): TypedValue =
        Value(DateTimeFormatter.ISO_DATE.format(localDate), xsdDate)
      implicit def instant2Value(instant: Instant): TypedValue =
        Value(instant.toString, xsdDateTime)

      // Using implicit conversions to TypedValues...
      val dateSetNode = NodeObject(
        dateSet -> Seq(
          LocalDate.of(2020, 1, 1),
          Instant.ofEpochMilli(0)
        )
      )

      // And it should map directly to typed values
      val nodeMap: Map[String, Node] = toMap(dateSetNode)
      nodeMap(dateSet.key.name) should be(
        SetObject(
          Seq(
            TypedValue("2020-01-01", xsdDate),
            TypedValue("1970-01-01T00:00:00Z", xsdDateTime)
          )
        )
      )
    }

    "should work with an iterable" in {
      val foaf = IRI("http://xmlns.com/foaf/0.1/").term("foaf")

      val givenName = schemaOrg("givenName").bindValue[String]

      // A set of string values
      val nick: SetBinding[String] = foaf("nick").bindSet[String]

      // Bind using iterable (rather than Seq / Set)
      val iterable: Iterable[String] = Set("Bill", "Billy", "Mac", "Buddy")
      val personWithNicknames = NodeObject(
        givenName -> "William",
        nick bind iterable
      )

      // And it should map directly to strings
      val nodeMap: Map[String, Node] = toMap(personWithNicknames)
      nodeMap(nick.key.name) should be(
        SetObject(
          Set(Value("Bill"), Value("Billy"), Value("Mac"), Value("Buddy"))
        )
      )
    }

    "should work with an immutable Seq" in {
      val foaf = IRI("http://xmlns.com/foaf/0.1/").term("foaf")

      val givenName = schemaOrg("givenName").bindValue[String]

      // A set of string values
      val nick: SetBinding[String] = foaf("nick").bindSet[String]

      // Immutable seq should work just the same
      val immutableSet = scala.collection.immutable.Set("Bill", "Billy", "Mac", "Buddy")
      val personWithNicknames = NodeObject(
        givenName -> "William",
        nick bind immutableSet
      )

      // And it should map directly to strings
      val nodeMap: Map[String, Node] = toMap(personWithNicknames)
      nodeMap(nick.key.name) should be(
        SetObject(
          Set(Value("Bill"), Value("Billy"), Value("Mac"), Value("Buddy"))
        )
      )
    }

    "a set of lists" in {
      val exampleOrg = IRI("https://example.org/").vocab

      val name    = exampleOrg("name").bindValue[String]
      val nodeSet = exampleOrg("nodeSet").bindSet[Node]

      // XXX Set is invariant, so unless we have a SetMapper we're going
      // to have to pass in straight nodes...
      // Should see if there's a covariant set somewhere...
      // https://www.scala-lang.org/old/node/9764
      val nodeSetNode = NodeObject(
        nodeSet -> Set(
          NodeObject(name -> "value1"),
          ListObject(Seq(Value("value2"))),
          SetObject(Set(Value("value3")))
        )
      )

      val nodeMap: Map[String, Node] = toMap(nodeSetNode)
      nodeMap(nodeSet.key.name) should be(
        SetObject(
          Set(
            NodeObject(name -> "value1"),
            ListObject(Seq(Value("value2"))),
            SetObject(Set(Value("value3")))
          )
        )
      )
    }

    "a set of nodes" in {
      val exampleOrg = IRI("https://example.org/").vocab

      val name    = exampleOrg("name").bindValue[String]
      val nodeSet = exampleOrg("nodeSet").bindSet[Node]

      // XXX Set is invariant, so unless we have a SetMapper we're going
      // to have to pass in straight nodes...
      // Should see if there's a covariant set somewhere...
      // https://www.scala-lang.org/old/node/9764
      val nodeSetNode = NodeObject(
        nodeSet -> Set(
          NodeObject(name -> "value1"),
          ListObject(Seq(Value("value2"))),
          SetObject(Set(Value("value3")))
        )
      )

      val nodeMap: Map[String, Node] = toMap(nodeSetNode)
      nodeMap(nodeSet.key.name) should be(
        SetObject(
          Set(
            NodeObject(name -> "value1"),
            ListObject(Seq(Value("value2"))),
            SetObject(Set(Value("value3")))
          )
        )
      )
    }

    "a set that can contain nulls" in {
      val exampleOrg = IRI("https://example.org/").vocab

      val nodeSet = exampleOrg("nodeSet").bindSet[Option[Node]]

      val nodeSetNode = NodeObject(
        nodeSet bind Set(
          Some(Value(1)),
          None
        )
      )

      val nodeMap: Map[String, Node] = toMap(nodeSetNode)
      nodeMap(nodeSet.key.name) should be(
        SetObject(
          Set(
            NumberLiteral(1),
            NullLiteral
          )
        )
      )
    }
  }

}
