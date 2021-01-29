package com.tersesystems.blindsight.jsonld

class NodeObjectBindingSpec extends BaseSpec {

  "A node object binding" when {
    "defined" should {
      "convert to a node object through a mapper" in {
        import java.util.Currency

        val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
        val value: ValueBinding[Int]   = schemaOrg("value").bindValue[Int]

        val monetaryAmountType = schemaOrg("MonetaryAmount")

        implicit val currencyMapper: ValueMapper[Currency] = ValueMapper { currency =>
          Value(currency.getCurrencyCode)
        }
        val currency: ValueBinding[Currency] = schemaOrg("currency").bindValue[Currency]

        case class MonetaryAmount(currency: Currency, value: Int)

        // Usually this would be inside the companion object
        implicit val monetaryAmountMapper: NodeObjectMapper[MonetaryAmount] = NodeObjectMapper {
          ma =>
            NodeObject(
              `@type`  -> monetaryAmountType,
              currency -> ma.currency,
              value    -> ma.value
            )
        }
        val estimatedSalary: NodeObjectBinding[MonetaryAmount] =
          schemaOrg("estimatedSalary").bindObject[MonetaryAmount]

        case class Occupation(estimatedSalary: MonetaryAmount, name: String)

        // Usually this would be inside the companion object
        implicit val occupationMapper: NodeObjectMapper[Occupation] = NodeObjectMapper { occ =>
          NodeObject(
            `@type`         -> schemaOrg("Occupation"),
            name            -> occ.name,
            estimatedSalary -> occ.estimatedSalary
          )
        }
        val hasOccupation: NodeObjectBinding[Occupation] =
          schemaOrg("hasOccupation").bindObject[Occupation]

        // Okay, here we go...
        val USD = Currency.getInstance("USD")
        val codeMonkeyPerson = NodeObject(
          `@type` -> schemaOrg("Person"),
          hasOccupation -> Occupation(
            name = "Code Monkey",
            estimatedSalary = MonetaryAmount(USD, 1)
          )
        )
        val nodeMap: Map[String, Node] = toMap(codeMonkeyPerson)

        nodeMap(`@type`.key.name) should be(Value("Person"))
        nodeMap(hasOccupation.key.name) should be(
          NodeObject(
            NodeEntry(`@type`.key.name, Value("Occupation")),
            NodeEntry(name.key.name, Value("Code Monkey")),
            NodeEntry(
              estimatedSalary.key.name,
              NodeObject(
                NodeEntry(`@type`.key.name, Value("MonetaryAmount")),
                NodeEntry(currency.key.name, Value("USD")),
                NodeEntry(value.key.name, Value(1))
              )
            )
          )
        )
      }
    }

    // Should represent an Option[Foo] with None as a UnitObject
    "render an optional node object as None" in {
      val optionalNode = schemaOrg("optionalNode").bindObject[Option[NodeObject]]

      val entry = optionalNode.bind(None)
      // XXX are there any situations where we'd want an empty node object rather than null?
      entry.value.head should be(NodeObject.Null)
    }
  }

  // XXX Add tests for bindObjects

}
