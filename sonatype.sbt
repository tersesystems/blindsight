sonatypeProfileName := "com.tersesystems"

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("tersesystems", "blindsight", "will@tersesystems.com"))

// https://github.com/sbt/sbt-pgp#configuration-signing-key
// tersesystems signing key
usePgpKeyHex("9033D60F5F798D53")
