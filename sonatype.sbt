// Project independent sonatype settings.

sonatypeProfileName := "com.tersesystems"

ThisBuild / developers := List(
  Developer(id="tersesystems", name="Terse Systems", email="will@tersesystems.com", url=url("https://tersesystems.com"))
)

// https://github.com/xerial/sbt-sonatype#buildsbt
ThisBuild / publishTo := sonatypePublishToBundle.value

// https://github.com/sbt/sbt-pgp#configuration-signing-key
// tersesystems signing key
usePgpKeyHex("9033D60F5F798D53")
