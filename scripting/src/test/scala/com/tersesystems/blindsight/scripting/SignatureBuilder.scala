package com.tersesystems.blindsight.scripting

import com.tersesystems.securitybuilder.MacBuilder

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

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
    val actual    = sha256Mac.doFinal(contents.getBytes(StandardCharsets.UTF_8))
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
    val len  = s.length
    val data = new Array[Byte](len / 2)
    var i    = 0
    while (i < len) {
      data(i / 2) =
        ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)).toByte
      i += 2
    }
    data
  }

}
