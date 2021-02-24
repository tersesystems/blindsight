package com.tersesystems.blindsight.jsonld

class MapBindingSpec extends BaseSpec {

  "a language map" should {
    "map using language keys" in {
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

      val nodeMap: Map[String, Node] = toMap(nodeObject)
      nodeMap(languageMap.key.name) should be(
        NodeObject(
          NodeEntry("en", StringLiteral("English")),
          NodeEntry("fr", StringLiteral("Français")),
          NodeEntry("de", StringLiteral("Deutsch")),
          NodeEntry("ar", StringLiteral("موبي ديك"))
        )
      )
    }

    "map using seq" in {
      val languageMap: LanguageMapBinding =
        schemaOrg("languageMap").bindLanguageMap

      val nodeObject: NodeObject = NodeObject(
        languageMap -> Map(
          "en" -> Seq("English", "Still English")
        )
      )

      val nodeMap: Map[String, Node] = toMap(nodeObject)
      nodeMap(languageMap.key.name) should be(
        NodeObject(
          NodeEntry("en", StringLiteral("English"), StringLiteral("Still English"))
        )
      )
    }

    "map using none index" in {
      val languageMap: LanguageMapBinding =
        schemaOrg("languageMap").bindLanguageMap

      val _: NodeObject = NodeObject(
        languageMap -> Map(
          Some("en") -> "English",
          None       -> "derp"
        )
      )

    }

    "map using Some / None and Seq" in {
      val languageMap: LanguageMapBinding =
        schemaOrg("languageMap").bindLanguageMap

      val nodeObject: NodeObject = NodeObject(
        languageMap -> Map(
          Some("en") -> Seq("English", "Still English"),
          None       -> Seq("Unknown")
        )
      )

      val nodeMap: Map[String, Node] = toMap(nodeObject)
      nodeMap(languageMap.key.name) should be(
        NodeObject(
          NodeEntry("en", StringLiteral("English"), StringLiteral("Still English")),
          NodeEntry("@none", StringLiteral("Unknown"))
        )
      )
    }
  }

  "An index map" should {
    "map using keys" in {
      val athletes   = schemaOrg("athletes").bindIndexMap
      val name       = schemaOrg("name").bindValue[String]
      val position   = schemaOrg("position").bindValue[String]
      val person     = schemaOrg("Person")
      val sportsTeam = schemaOrg("SportsTeam")

      val indexMapNode = NodeObject(
        name    -> "San Francisco Giants",
        `@type` -> sportsTeam,
        athletes -> Map(
          "catcher" -> NodeObject(
            `@type`  -> person,
            name     -> "Buster Posey",
            position -> "Catcher"
          ),
          "pitcher" -> NodeObject(
            `@type`  -> person,
            name     -> "Madison Bumgarner",
            position -> "Starting Pitcher"
          )
        )
      )

      val nodeMap: Map[String, Node] = toMap(indexMapNode)
      nodeMap(athletes.key.name) should be(
        NodeObject(
          NodeEntry(
            "catcher",
            NodeObject(
              NodeEntry(
                `@type`.key.name,
                StringLiteral("Person")
              ),
              NodeEntry(name.key.name, StringLiteral("Buster Posey")),
              NodeEntry(position.key.name, StringLiteral("Catcher"))
            )
          ),
          NodeEntry(
            "pitcher",
            NodeObject(
              NodeEntry(
                `@type`.key.name,
                StringLiteral("Person")
              ),
              NodeEntry(
                name.key.name,
                StringLiteral("Madison Bumgarner")
              ),
              NodeEntry(
                position.key.name,
                StringLiteral("Starting Pitcher")
              )
            )
          )
        )
      )
    }

    "map using optional index" in {
      val optIndexMap = schemaOrg("optionalIndexMap").bindIndexMap

      val indexMapNode = NodeObject(
        optIndexMap -> Map(
          Some("existing") -> "existingValue",
          None             -> "defaultValue"
        )
      )
      val nodeMap: Map[String, Node] = toMap(indexMapNode)
      val node                       = nodeMap(optIndexMap.key.name).asInstanceOf[NodeObject]

      node.value should contain theSameElementsAs Seq(
        NodeEntry("existing", StringLiteral("existingValue")),
        NodeEntry("@none", StringLiteral("defaultValue"))
      )
    }

    "map using optional value using MapBindingInput" in {
      val optIndexMap = schemaOrg("optionalIndexMap").bindIndexMap

      val indexMapNode = NodeObject(
        optIndexMap -> Map(
          Some("existing") -> Option("existingValue"),
          None             -> None
        )
      )
      val nodeMap: Map[String, Node] = toMap(indexMapNode)
      val node                       = nodeMap(optIndexMap.key.name).asInstanceOf[NodeObject]

      node.value should contain theSameElementsAs Seq(
        NodeEntry("existing", StringLiteral("existingValue")),
        NodeEntry("@none", NullLiteral)
      )
    }

    "map using optional value and seq input" in {
      val optIndexMap = schemaOrg("optionalIndexMap").bindIndexMap

      val indexMapNode = NodeObject(
        optIndexMap -> Map(
          Some("existing") -> Seq("existingValue", "another value"),
          None             -> Seq("none")
        )
      )
      val nodeMap: Map[String, Node] = toMap(indexMapNode)
      val node                       = nodeMap(optIndexMap.key.name).asInstanceOf[NodeObject]

      node.value should contain theSameElementsAs Seq(
        NodeEntry("existing", StringLiteral("existingValue"), StringLiteral("another value")),
        NodeEntry("@none", StringLiteral("none"))
      )
    }

    "map using optional value and optional seq input" in {
      val optIndexMap = schemaOrg("optionalIndexMap").bindIndexMap

      val indexMapNode = NodeObject(
        optIndexMap -> Map(
          Some("existing") -> Seq(Some("existingValue"), Some("another value")),
          None             -> Seq(None)
        )
      )
      val nodeMap: Map[String, Node] = toMap(indexMapNode)
      val node                       = nodeMap(optIndexMap.key.name).asInstanceOf[NodeObject]

      node.value should contain theSameElementsAs Seq(
        NodeEntry("existing", StringLiteral("existingValue"), StringLiteral("another value")),
        NodeEntry("@none", NullLiteral)
      )
    }
  }

  "An id map" should {
    "map using relative IRIs" in {
      // https://www.w3.org/TR/json-ld11/#node-identifier-indexing
      /*
        "@id": "http://example.com/",
        "@type": "schema:Blog",
        "name": "World Financial News",
        "post": {
          "1/en": {
            "body": "World commodities were up today with heavy trading of crude oil...",
            "words": 1539
          },
          "1/de": {
            "body": "Die Werte an Warenbörsen stiegen im Sog eines starken Handels von Rohöl...",
            "words": 1204
          }
        }
       */

      val `@type`                    = Keyword.`@type`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val body: ValueBinding[String] = schemaOrg("body").bindValue[String]
      val words: ValueBinding[Int]   = schemaOrg("words").bindValue[Int]

      val post       = schemaOrg("post").bindIdMap
      val exampleCom = IRI("http://example.com/")
      val baseId     = exampleCom.base
      val node = NodeObject(
        `@id`   -> exampleCom,
        `@type` -> schemaOrg("Blog"),
        name    -> "World Financial News",
        post -> Map(
          baseId("1/en") -> NodeObject(
            body  -> "World commodities were up today with heavy trading of crude oil...",
            words -> 1539
          ),
          baseId("1/de") -> NodeObject(
            body  -> "Die Werte an Warenbörsen stiegen im Sog eines starken Handels von Rohöl...",
            words -> 1204
          )
        )
      )

      val nodeMap: Map[String, Node] = toMap(node)
      nodeMap(post.key.name) should be(
        NodeObject(
          NodeEntry(
            "1/en",
            NodeObject(
              NodeEntry(
                body.key.name,
                StringLiteral(
                  "World commodities were up today with heavy trading of crude oil..."
                )
              ),
              NodeEntry(words.key.name, NumberLiteral(1539))
            )
          ),
          NodeEntry(
            "1/de",
            NodeObject(
              NodeEntry(
                body.key.name,
                StringLiteral(
                  "Die Werte an Warenbörsen stiegen im Sog eines starken Handels von Rohöl..."
                )
              ),
              NodeEntry(words.key.name, NumberLiteral(1204))
            )
          )
        )
      )
    }

    "map using absolute IRIs" in {
      val `@type`                    = Keyword.`@type`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val words: ValueBinding[Int]   = schemaOrg("words").bindValue[Int]

      val post       = schemaOrg("post").bindIdMap
      val exampleCom = IRI("http://example.com/")
      val node = NodeObject(
        `@id`   -> exampleCom,
        `@type` -> schemaOrg("Blog"),
        name    -> "World Financial News",
        post -> Map(
          exampleCom -> NodeObject(
            words -> 1539
          )
        )
      )

      val nodeMap: Map[String, Node] = toMap(node)
      nodeMap(post.key.name) should be(
        NodeObject(
          NodeEntry(
            "http://example.com/",
            NodeObject(NodeEntry(words.key.name, NumberLiteral(1539)))
          )
        )
      )
    }

    "map using Seq" in {
      val `@type`                    = Keyword.`@type`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val words: ValueBinding[Int]   = schemaOrg("words").bindValue[Int]

      val post               = schemaOrg("post").bindIdMap
      val iri                = IRI("http://example.com/")
      val exampleTerm        = iri.term("example")
      val baseId: CompactIRI = exampleTerm("baseId")
      val node = NodeObject(
        `@id`   -> iri,
        `@type` -> schemaOrg("Blog"),
        name    -> "World Financial News",
        post -> Map(
          baseId -> Seq(NodeObject(words -> 1539))
        )
      )

      val nodeMap: Map[String, Node] = toMap(node)
      nodeMap(post.key.name) should be(
        NodeObject(
          NodeEntry(
            "example:baseId",
            NodeObject(NodeEntry(words.key.name, NumberLiteral(1539)))
          )
        )
      )
    }

    "map using optional keys and Option[Seq]" in {
      val `@type`                    = Keyword.`@type`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val words: ValueBinding[Int]   = schemaOrg("words").bindValue[Int]

      val post        = schemaOrg("post").bindIdMap
      val exampleIRI  = IRI("http://example.com/")
      val exampleTerm = exampleIRI.term("example")
      val baseId      = exampleTerm("baseId")
      val node = NodeObject(
        `@id`   -> exampleIRI,
        `@type` -> schemaOrg("Blog"),
        name    -> "World Financial News",
        post -> Map(
          Some(baseId) -> Seq(NodeObject(words -> 1539)),
          None         -> Seq(NodeObject(words -> 0))
        )
      )

      val nodeMap: Map[String, Node] = toMap(node)
      nodeMap(post.key.name) should be(
        NodeObject(
          NodeEntry(
            "example:baseId",
            NodeObject(NodeEntry(words.key.name, NumberLiteral(1539)))
          ),
          NodeEntry(
            "@none",
            NodeObject(NodeEntry(words.key.name, NumberLiteral(0)))
          )
        )
      )
    }

    "map using optional keys" in {
      val `@type`                    = Keyword.`@type`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val body: ValueBinding[String] = schemaOrg("body").bindValue[String]
      val words: ValueBinding[Int]   = schemaOrg("words").bindValue[Int]

      val post       = schemaOrg("post").bindIdMap
      val exampleCom = IRI("http://example.com/")
      val baseId     = exampleCom.base
      val node = NodeObject(
        `@id`   -> exampleCom,
        `@type` -> schemaOrg("Blog"),
        name    -> "World Financial News",
        post -> Map(
          Some(baseId("1/en")) -> NodeObject(
            body  -> "World commodities were up today with heavy trading of crude oil...",
            words -> 1539
          ),
          None -> NodeObject(
            body  -> "Die Werte an Warenbörsen stiegen im Sog eines starken Handels von Rohöl...",
            words -> 1204
          )
        )
      )

      val nodeMap: Map[String, Node] = toMap(node)
      nodeMap(post.key.name) should be(
        NodeObject(
          NodeEntry(
            "1/en",
            NodeObject(
              NodeEntry(
                body.key.name,
                StringLiteral(
                  "World commodities were up today with heavy trading of crude oil..."
                )
              ),
              NodeEntry(words.key.name, NumberLiteral(1539))
            )
          ),
          NodeEntry(
            "@none",
            NodeObject(
              NodeEntry(
                body.key.name,
                StringLiteral(
                  "Die Werte an Warenbörsen stiegen im Sog eines starken Handels von Rohöl..."
                )
              ),
              NodeEntry(words.key.name, NumberLiteral(1204))
            )
          )
        )
      )
    }
  }

  "A type map" should {
    "map using IRI" in {
      // https://www.w3.org/TR/json-ld11/#node-type-indexing
      /*
      {
        "name": "Manu Sporny",
        "affiliation": {
          "schema:Corporation": {
            "@id": "https://digitalbazaar.com/",
            "name": "Digital Bazaar"
          },
          "schema:ProfessionalService": {
            "@id": "https://spec-ops.io",
            "name": "Spec-Ops"
          }
        }
       */
      val schemaTerm = IRI("https://schema.org/").term("schema")

      val `@id`                      = Keyword.`@id`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val affiliation =
        schemaOrg("affiliation").bindTypeMap
      val node = NodeObject(
        affiliation -> Map(
          schemaTerm("Corporation") -> NodeObject(
            `@id` -> IRI("https://digitalbazaar.com/"),
            name  -> "Digital Bazaar"
          ),
          schemaTerm("ProfessionalService") -> NodeObject(
            `@id` -> IRI("https://spec-ops.io"),
            name  -> "Spec-Ops"
          )
        )
      )

      val nodeMap: Map[String, Node] = toMap(node)
      nodeMap(affiliation.key.name) should be(
        NodeObject(
          NodeEntry(
            "schema:Corporation",
            NodeObject(
              NodeEntry(
                `@id`.key.name,
                StringLiteral(
                  "https://digitalbazaar.com/"
                )
              ),
              NodeEntry(
                name.key.name,
                StringLiteral(
                  "Digital Bazaar"
                )
              )
            )
          ),
          NodeEntry(
            "schema:ProfessionalService",
            NodeObject(
              NodeEntry(
                `@id`.key.name,
                StringLiteral(
                  "https://spec-ops.io"
                )
              ),
              NodeEntry(
                name.key.name,
                StringLiteral(
                  "Spec-Ops"
                )
              )
            )
          )
        )
      )
    }

    "map with a seq of node objects" in {
      val schemaTerm = IRI("https://schema.org/").term("schema")

      val `@id`                      = Keyword.`@id`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val affiliation                = schemaOrg("affiliation").bindTypeMap
      val _ = NodeObject(
        affiliation -> Map(
          schemaTerm("Corporation") -> Seq(
            NodeObject(
              `@id` -> IRI("https://corp1.com/"),
              name  -> "Corporation One"
            ),
            NodeObject(
              `@id` -> IRI("https://corp2.io"),
              name  -> "Corporation 2"
            )
          )
        )
      )
    }

    "map with none" in {
      val schemaTerm = IRI("https://schema.org/").term("schema")

      val `@id`                      = Keyword.`@id`.bindIRI
      val name: ValueBinding[String] = schemaOrg("name").bindValue[String]
      val affiliation                = schemaOrg("affiliation").bindTypeMap
      val _ = NodeObject(
        affiliation -> Map(
          Some(schemaTerm("Corporation")) -> NodeObject(
            `@id` -> IRI("https://corp1.com/"),
            name  -> "Corporation One"
          ),
          None -> NodeObject(
            `@id` -> IRI("https://corpnone.com/"),
            name  -> "Corporation None"
          )
        )
      )
    }
  }
}
