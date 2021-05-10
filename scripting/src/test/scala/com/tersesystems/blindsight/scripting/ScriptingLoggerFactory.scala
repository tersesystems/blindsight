package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.securitybuilder.MacBuilder
import com.twineworks.tweakflow.lang.errors.LangException

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import java.util
import java.util.stream.Collectors

/**
 *
 */
class ScriptingLoggerFactory extends LoggerFactory {

  // Secret passphrase that is never passed around in the clear, so an attacker
  // can't generate a valid signature :-)
  private val privateString = "very secret key"

  private val signatureBuilder = new SignatureBuilder(privateString)

  private val scriptFile: Path = Paths.get("src/test/tweakflow/condition.tf").toAbsolutePath
  private val signatureFile: Path = Paths.get("src/test/tweakflow/condition.tf.asc").toAbsolutePath

  // Uncomment this to start signing the script on program start
  writeString(
    signatureFile,
    signatureBuilder.sign(readString(scriptFile))
  )

  // you can disable the verifier by setting input => true
  // input => signatureBuilder.verify(input, Files.readString(signatureFile))
  private val fileHandle = new FileScriptHandle(scriptFile,
    input => signatureBuilder.verify(input, readString(signatureFile)), {
      case e@(lang: LangException) =>
        val info = lang.getSourceInfo
        if (info != null) {
          println(s"info = $info")
        }
        e.printStackTrace()
    })
  private val cm = new ScriptManager(fileHandle)

  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new ScriptAwareLogger(CoreLogger(underlying), cm)
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
    val lines = util.Arrays.asList(contents)
    Files.write(path, lines)
  }
}

class SignatureBuilder(privateString: String) {

  /**
   * Creates a signature incorporating the contents and the private string.
   *
   * @param contents the contents to create a SHA-HMAC.
   * @return a hexidecimal version of the MAC
   */
  def sign(contents: String): String = {
    // Regenerate the MAC (you would put this somewhere that people can't touch it usually, so an
    // attacker can't swap out the script and the MAC at once)
    val sha256Mac = MacBuilder.builder.withHmacSHA256().withString(privateString).build
    byteArrayToHex(sha256Mac.doFinal(contents.getBytes(StandardCharsets.UTF_8)))
  }

  /**
   * Verifies that the contents contains a MAC that matches privateString
   *
   * @param contents the file contents
   * @param signatureHex the signature
   * @return true if the MAC matches
   */
  def verify(contents: String, signatureHex: String): Boolean = {
    val sha256Mac = MacBuilder.builder.withHmacSHA256().withString(privateString).build
    val actual = sha256Mac.doFinal(contents.getBytes(StandardCharsets.UTF_8))
    val signature = hexToByteArray(signatureHex)
    MessageDigest.isEqual(actual, signature)
  }

  private def byteArrayToHex(a: Array[Byte]): String = {
    val sb = new StringBuilder(a.length * 2)
    for (b <- a) {
      sb.append(String.format("%02x", b))
    }
    sb.toString
  }

  private def hexToByteArray(s: String): Array[Byte] = {
    val len = s.length
    val data = new Array[Byte](len / 2)
    var i = 0
    while (i < len) {
      data(i / 2) = ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)).toByte
      i += 2
    }
    data
  }

}
