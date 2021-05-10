package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.fixtures.OneContextPerTest
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ScriptingSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  override def resourceName: String = "/logback-test.xml"

  "scripting logger" should {
      "fail" in {
        fail()
      }
  }

}
