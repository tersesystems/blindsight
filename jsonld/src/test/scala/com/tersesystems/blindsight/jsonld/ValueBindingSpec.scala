package com.tersesystems.blindsight.jsonld

import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.bobj

import java.time.format.DateTimeFormatter

class ValueBindingSpec extends BaseSpec {

  "A string binding" when {
    "defined" should {
      "convert to a value" in {
        val givenName = schemaOrg("givenName").bindValue[String]

        val willPerson = NodeObject(
          givenName -> "Person"
        )

        val map: Map[String, Node] = toMap(willPerson)
        map(givenName.key.name) should be(StringLiteral("Person"))
      }
    }

    "defined with language" should {
      "convert to a string value" in {
        val givenName = schemaOrg("givenName").bindValue[String]
        val willPerson = NodeObject(
          givenName -> "Steve", // literals work
          givenName -> Value("Will", StringDirection.LeftToRight, "en") // values work
        )

        val map: Map[String, Node] = toMap(willPerson)
        map(givenName.key.name) should be(
          StringValue("Will", Some(StringDirection.LeftToRight), Some("en"))
        )
      }

      "only accept a string value" in {
        val givenName = schemaOrg("givenName").bindValue[StringValue]
        val willPerson = NodeObject(
          // givenName -> "Will", <-- literals won't work here
          givenName -> Value("Will", StringDirection.LeftToRight, "en")
        )

        val map: Map[String, Node] = toMap(willPerson)
        map(givenName.key.name) should be(
          StringValue("Will", Some(StringDirection.LeftToRight), Some("en"))
        )
      }
    }

    "not defined" should {
      "convert to unit" in {
        val givenName = schemaOrg("givenName").bindValue[Option[String]]

        val willPerson = NodeObject(
          `@type`   -> schemaOrg("Person"),
          givenName -> None
        )

        val map: Map[String, Node] = toMap(willPerson)

        map(`@type`.key.name) should be(Value("Person"))
        map(givenName.key.name) should be(Value.none)
      }
    }
  }

  "A number binding" when {
    "defined" should {
      "convert to a value" in {
        val currency: ValueBinding[String] = schemaOrg("currency").bindValue[String]
        val value: ValueBinding[Int]       = schemaOrg("value").bindValue[Int]

        val oneDollar = NodeObject(
          `@type`  -> schemaOrg("MonetaryAmount"),
          currency -> "USD",
          value    -> 1
        )
        val map: Map[String, Node] = toMap(oneDollar)

        map(`@type`.key.name) should be(Value("MonetaryAmount"))
        map(currency.key.name) should be(Value("USD"))
        map(value.key.name) should be(Value(1))
      }
    }

    "not defined" should {
      "convert to a value" in {
        val value = schemaOrg("value").bindValue[Option[Int]]

        val oneDollar = NodeObject(
          `@type` -> schemaOrg("MonetaryAmount"),
          value   -> None
        )
        val map: Map[String, Node] = toMap(oneDollar)

        map(`@type`.key.name) should be(Value("MonetaryAmount"))
        map(value.key.name) should be(Value.none)
      }
    }
  }

  "A boolean binding" when {
    "defined" should {
      "convert to a value" in {
        val name          = schemaOrg("name").bindValue[String]
        val abridged      = schemaOrg("abridged").bindValue[Boolean]
        val numberOfPages = schemaOrg("numberOfPages").bindValue[Int]

        val abridgedMobyDick = NodeObject(
          `@type`       -> schemaOrg("Book"),
          name          -> "Moby Dick",
          abridged      -> true,
          numberOfPages -> 12
        )
        val map: Map[String, Node] = toMap(abridgedMobyDick)

        map(`@type`.key.name) should be(Value("Book"))
        map(abridged.key.name) should be(Value(true))
        map(numberOfPages.key.name) should be(Value(12))
      }
    }
  }

  "A typed value binding" when {
    "defined" should {
      "convert to a value through a value mapper" in {
        import java.time.LocalDate

        // Set up term and compact IRI
        val xsd     = IRI("http://www.w3.org/2001/XMLSchema#").term("xsd")
        val xsdDate = xsd("date")

        // Define a value mapper here
        implicit val localDateMapper: ValueMapper[LocalDate] = ValueMapper { date =>
          Value(DateTimeFormatter.ISO_DATE.format(date), xsdDate)
        }

        val name        = schemaOrg("name").bindValue[String]
        val dateCreated = schemaOrg("dateCreated").bindValue[LocalDate]

        val abridgedMobyDick = NodeObject(
          `@type`     -> schemaOrg("Book"),
          name        -> "Moby Dick",
          dateCreated -> LocalDate.of(2020, 1, 1)
        )
        val map: Map[String, Node] = toMap(abridgedMobyDick)

        map(`@type`.key.name) should be(Value("Book"))
        map(name.key.name) should be(Value("Moby Dick"))
        map(dateCreated.key.name) should be(TypedValue("2020-01-01", xsdDate))
      }

      "Can be defined directly" in {
        import java.time.LocalDate

        // Set up term and compact IRI
        val xsd     = IRI("http://www.w3.org/2001/XMLSchema#").term("xsd")
        val xsdDate = xsd("date")

        val name        = schemaOrg("name").bindValue[String]
        val dateCreated = schemaOrg("dateCreated").bindValue[TypedValue]
        val localDate   = LocalDate.of(2020, 1, 1)
        val abridgedMobyDick = NodeObject(
          `@type`     -> schemaOrg("Book"),
          name        -> "Moby Dick",
          dateCreated -> Value(DateTimeFormatter.ISO_DATE.format(localDate), xsdDate)
        )
        val map: Map[String, Node] = toMap(abridgedMobyDick)

        map(`@type`.key.name) should be(Value("Book"))
        map(name.key.name) should be(Value("Moby Dick"))
        map(dateCreated.key.name) should be(TypedValue("2020-01-01", xsdDate))
      }
    }
  }

  "A JSON value" when {
    "defined" should {
      "work using raw bobject" in {
        val jsonTerm = schemaOrg("jsonValue").bindValue[BObject]

        import com.tersesystems.blindsight.DSL._
        val bobject = bobj("key" -> "value")

        val nodeObject = NodeObject(jsonTerm -> bobject)
        val map        = toMap(nodeObject)
        map(jsonTerm.key.name) should be(
          JsonObjectLiteral(BObject(List(BField("key", BString("value")))))
        )
      }

      "work using raw barray" in {
        val jsonTerm = schemaOrg("jsonValue").bindValue[BArray]

        import com.tersesystems.blindsight.DSL._
        val barray = BArray(List(1, 2, 3))

        val nodeObject = NodeObject(jsonTerm -> barray)
        val map        = toMap(nodeObject)
        map(jsonTerm.key.name) should be(JsonArrayLiteral(BArray(List(BInt(1), BInt(2), BInt(3)))))
      }
    }
  }

  // XXX Add tests for bindValues
  "bindValues" should {
    "work" in {
      val givenName = schemaOrg("givenName").bindValues[String]

      val willPerson = NodeObject(
        givenName -> Seq("Me", "You")
      )

      val entries: NodeEntry = willPerson.value.head
      entries.value should contain theSameElementsInOrderAs(Seq(StringLiteral("Me"), StringLiteral("You")))
    }
  }

}
