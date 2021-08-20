package com.tersesystems.blindsight.jsonld

import com.tersesystems.blindsight.AST._

class BlindsightASTMappingSpec extends BaseSpec {

  "BObjectMapping" should {

    "map a simple string" in {
      import BlindsightASTMapping._

      val givenName = schemaOrg("givenName").bindValue[String]

      val bobject = toBObject(NodeObject(givenName -> "Will"))
      bobject shouldBe BObject(List(BField("givenName", BString("Will"))))
    }

    "map a simple int" in {
      import BlindsightASTMapping._

      val age     = schemaOrg("age").bindValue[Int]
      val bobject = toBObject(NodeObject(age -> 12))
      bobject shouldBe BObject(List(BField("age", BInt(12))))
    }

    "map a simple float" in {
      import BlindsightASTMapping._

      val confidence = schemaOrg("confidence").bindValue[Float]
      val bobject    = toBObject(NodeObject(confidence -> 99.5f))
      bobject shouldBe BObject(List(BField("confidence", BDecimal(99.5))))
    }

    "map a simple double" in {
      import BlindsightASTMapping._

      val confidence = schemaOrg("confidence").bindValue[Double]
      val bobject    = toBObject(NodeObject(confidence -> 99.5))
      bobject shouldBe BObject(List(BField("confidence", BDouble(99.5))))
    }

    "map a simple optional value" in {
      import BlindsightASTMapping._

      val middleName = schemaOrg("middleName").bindValue[Option[String]]
      val bobject    = toBObject(NodeObject(middleName -> None))
      bobject shouldBe BObject(List(BField("middleName", BNull)))
    }

    "map a simple node object" in {
      import BlindsightASTMapping._

      val `@type`   = Keyword.`@type`.bindIRI
      val givenName = schemaOrg("givenName").bindValue[String]
      val employee  = schemaOrg("employee").bindObject[NodeObject]
      val bobject = toBObject(
        NodeObject(
          employee -> NodeObject(
            givenName -> "Will",
            `@type`   -> schemaOrg("Person")
          )
        )
      )
      bobject shouldBe BObject(
        List(
          BField(
            "employee",
            BObject(
              List(
                BField("givenName", BString("Will")),
                BField("@type", BString("Person"))
              )
            )
          )
        )
      )
    }

    "map a simple optional node object" in {
      import BlindsightASTMapping._

      val optionalPerson = schemaOrg("optionalPerson").bindObject[Option[NodeObject]]
      val bobject        = toBObject(NodeObject(optionalPerson -> None))
      bobject shouldBe BObject(List(BField("optionalPerson", BNull)))
    }

    "map a list object" in {
      import BlindsightASTMapping._

      val stringList = schemaOrg("stringList").bindList[String]

//        [error] /home/wsargent/work/blindsight/jsonld/src/test/scala/com/tersesystems/blindsight/jsonld/BlindsightASTMappingSpec.scala:91:56: type mismatch;
//      [error]  found   : (com.tersesystems.blindsight.jsonld.ListBinding[String], Seq[String])
//        [error]  required: com.tersesystems.blindsight.jsonld.NodeEntry
//        [error]       val bobject    = toBObject(NodeObject(stringList -> Seq("1", "2", "3")))
//        [error]                                                        ^

      val bobject = toBObject(NodeObject(stringList bind Iterable("1", "2", "3")))
      bobject shouldBe BObject(
        List(
          BField(
            "stringList",
            BObject(
              List(BField("@list", BArray(List(BString("1"), BString("2"), BString("3")))))
            )
          )
        )
      )
    }

    "map an optional list object" in {
      import BlindsightASTMapping._

      val stringList = schemaOrg("stringList").bindList[Option[String]]
      val bobject    = toBObject(NodeObject(stringList -> Seq(Some("1"), None, Some("3"))))
      bobject shouldBe BObject(
        List(
          BField(
            "stringList",
            BObject(
              List(BField("@list", BArray(List(BString("1"), BNull, BString("3")))))
            )
          )
        )
      )
    }

    "map a set object" in {
      import BlindsightASTMapping._

      val stringSet = schemaOrg("stringSet").bindSet[String]
      val bobject   = toBObject(NodeObject(stringSet -> Seq("1", "2", "3")))
      bobject shouldBe BObject(
        List(
          BField(
            "stringSet",
            BObject(
              List(BField("@set", BArray(List(BString("1"), BString("2"), BString("3")))))
            )
          )
        )
      )
    }

    "map a set object with a null as element" in {
      import BlindsightASTMapping._

      val stringSet = schemaOrg("stringSet").bindSet[Option[String]]
      val bobject   = toBObject(NodeObject(stringSet -> Seq(Some("1"), None, Some("3"))))
      bobject shouldBe BObject(
        List(
          BField(
            "stringSet",
            BObject(
              List(BField("@set", BArray(List(BString("1"), BNull, BString("3")))))
            )
          )
        )
      )
    }

    "map an index map" in {
      import BlindsightASTMapping._

      val indexMap = schemaOrg("indexMap").bindIndexMap
      val bobject  = toBObject(NodeObject(indexMap -> Map("one" -> 1, "two" -> 2)))
      bobject shouldBe BObject(
        List(
          BField(
            "indexMap",
            BObject(
              List(BField("one", BInt(1)), BField("two", BInt(2)))
            )
          )
        )
      )
    }

    "map a language map" in {
      import BlindsightASTMapping._

      val indexMap: LanguageMapBinding = schemaOrg("indexMap").bindLanguageMap
      val bobject = toBObject(NodeObject(indexMap -> Map("en" -> "English", "he" -> "Hebrew")))
      bobject shouldBe BObject(
        List(
          BField(
            "indexMap",
            BObject(
              List(
                BField("en", BString("English")),
                BField("he", BString("Hebrew"))
              )
            )
          )
        )
      )
    }
  }

}
