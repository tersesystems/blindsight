sonatypeProfileName := "com.tersesystems"

publishMavenStyle := true

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