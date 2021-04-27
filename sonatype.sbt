sonatypeProfileName := "com.tersesystems"

// https://github.com/sbt/sbt-pgp#configuration-signing-key
usePgpKeyHex("9033D60F5F798D53")

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("tersesystems", "blindsight", "will@tersesystems.com"))

import ReleaseTransformations._

releaseCrossBuild := true // true if you cross-build the project for multiple Scala versions
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)