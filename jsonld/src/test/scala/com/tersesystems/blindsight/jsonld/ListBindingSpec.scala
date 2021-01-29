package com.tersesystems.blindsight.jsonld

class ListBindingSpec extends BaseSpec with MyGeoContext {

  "A list" should {

    "resolve values" in {
      val listOfStrings = schemaOrg("listOfStrings").bindList[String]

      val aListOfStrings = NodeObject(
        listOfStrings bind Seq("One", "Two", "Three", "Four")
      )

      val nodeMap: Map[String, Node] = toMap(aListOfStrings)
      nodeMap(listOfStrings.key.name) should be(
        ListObject(
          Seq(Value("One"), Value("Two"), Value("Three"), Value("Four"))
        )
      )
    }

    "resolve options to some or none" in {
      val optionalStrings = schemaOrg("optionalStrings").bindList[Option[String]]

      val obj = NodeObject(
        optionalStrings bind Seq(
          Some("some"),
          None
        )
      )
      val nodeMap: Map[String, Node] = toMap(obj)
      nodeMap(optionalStrings.key.name) should be(
        ListObject(
          Seq(Value("some"), NullLiteral)
        )
      )
    }

    "resolve node objects" in {
      val name        = schemaOrg("name").bindValue[String]
      val listOfNodes = schemaOrg("listOfNodes").bindList[NodeObject]

      val listObject = NodeObject(
        listOfNodes bind Seq(
          NodeObject(name -> "firstNode"),
          NodeObject(name -> "secondNode")
        )
      )

      val nodeMap: Map[String, Node] = toMap(listObject)
      nodeMap(listOfNodes.key.name) should be(
        ListObject(
          Seq(
            NodeObject(name -> "firstNode"),
            NodeObject(name -> "secondNode")
          )
        )
      )
    }

    "resolve list of lists" in {
      val geoFeatureNode = NodeObject(
        `@type` -> schemaOrg("Feature"),
        bbox    -> Seq(-10.0, -10.0, 10.0, 10.0),
        geometry -> Geometry(
          "Polygon",
          Seq(
            Seq(-10.0, -10.0),
            Seq(10.0, -10.0),
            Seq(10.0, 10.0),
            Seq(-10.0, 10.0)
          )
        )
      )

      val nodeMap: Map[String, Node] = toMap(geoFeatureNode)
      val geometryNode               = nodeMap(geometry.key.name)
      val geometryMap                = toMap(geometryNode.asInstanceOf[NodeObject])
      geometryMap(coordinates.key.name) should be(
        ListObject(
          Seq(
            ListObject(Seq(NumberLiteral(-10.0), NumberLiteral(-10.0))),
            ListObject(Seq(NumberLiteral(10.0), NumberLiteral(-10.0))),
            ListObject(Seq(NumberLiteral(10.0), NumberLiteral(10.0))),
            ListObject(Seq(NumberLiteral(-10.0), NumberLiteral(10.0)))
          )
        )
      )
    }
  }

}

trait MyGeoContext {
  // geojson has coordinates, which are lists of lists:
  // https://w3c.github.io/json-ld-syntax/#example-83-coordinates-expressed-in-geojson
  val geoJson    = IRI("https://purl.org/geojson/vocab#").vocab
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
      `@type`     -> geoJson(geo.`@type`),
      coordinates -> geo.coords
    )
  }
}
