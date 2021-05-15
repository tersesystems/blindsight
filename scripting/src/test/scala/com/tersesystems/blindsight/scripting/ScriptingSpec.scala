package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.twineworks.tweakflow.lang.errors.LangException
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

class ScriptingSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  override def resourceName: String = "/logback-test.xml"

  import com.tersesystems.blindsight.LoggerResolver
  implicit val logbackLoggerToLoggerResolver: LoggerResolver[ch.qos.logback.classic.Logger] = {
    LoggerResolver(identity)
  }

  "scripting logger" should {
    "work with a simple script" in {
      val scriptHandle = new ScriptHandle {
        override def isInvalid: Boolean = false

        override val script: String =
          readString(Paths.get("scripting/src/test/tweakflow/condition.tf"))

        override def report(e: Throwable): Unit = e.printStackTrace()
      }
      val sm            = new ScriptManager(scriptHandle);
      val loggerFactory = new ScriptingLoggerFactory(sm)
      val underlying    = loggerContext.getLogger(this.getClass)
      val logger        = loggerFactory.getLogger(underlying)

      logger.info("Hello world!")
      logger.debug("Should not be visible")

      listAppender.list.size must be(1)
      val event = listAppender.list.get(0)
      event.getMessage must equal("Hello world!")
    }

    "work with a MAC script" in {
      // Secret passphrase that is never passed around in the clear, so an attacker
      // can't generate a valid signature :-)
      val privateString    = "very secret key"
      val signatureBuilder = new SignatureBuilder(privateString)
      val scriptFile: Path = Paths.get("scripting/src/test/tweakflow/condition.tf").toAbsolutePath
      val signatureFile: Path =
        Paths.get("scripting/src/test/tweakflow/condition.tf.asc").toAbsolutePath

      writeString(
        signatureFile,
        signatureBuilder.sign(readString(scriptFile))
      )

      val scriptHandle = new FileScriptHandle(
        scriptFile,
        input => signatureBuilder.verify(input, readString(signatureFile)),
        {
          case e @ (lang: LangException) =>
            val info = lang.getSourceInfo
            if (info != null) {
              println(s"info = $info")
            }
            e.printStackTrace()
          case e =>
            e.printStackTrace()
        }
      )
      val sm            = new ScriptManager(scriptHandle);
      val loggerFactory = new ScriptingLoggerFactory(sm)
      val underlying    = loggerContext.getLogger(this.getClass)
      val logger        = loggerFactory.getLogger(underlying)

      logger.info("Hello world!")
      logger.debug("Should not be visible")

      listAppender.list.size must be(1)
      val event = listAppender.list.get(0)
      event.getMessage must equal("Hello world!")
    }
  }

  protected def readString(path: Path): String = {
    val reader = Files.newBufferedReader(path)
    try {
      reader.lines().collect(Collectors.joining("\n"))
    } finally {
      reader.close()
    }
  }

  protected def writeString(path: Path, contents: String): Unit = {
    val lines = java.util.Arrays.asList(contents)
    Files.write(path, lines)
  }

}
