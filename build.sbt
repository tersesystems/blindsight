import Dependencies._
import sbt.Keys.libraryDependencies

lazy val scala213 = "2.13.1"
lazy val scala212 = "2.12.11"
lazy val scala211 = "2.11.12"
ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212, scala213)

ThisBuild / scalafmtOnCompile := true

// These settings seem not to work for sbt-release-early, so .travis.yml copies to
// .sbt/gpg/pubring.asc / secring.asc as a fallback
ThisBuild / pgpPublicRing := file(".travis/local.pubring.asc")
ThisBuild / pgpSecretRing := file(".travis/local.secring.asc")

// https://github.com/jvican/sbt-release-early/wiki/How-to-release-with-Bintray#releasing-to-maven-central
// Disable sync to maven, we absolutely don't need this in sbt
// and it causes a None.get error at bintray.BintrayRepo.$anonfun$requestSonatypeCredentials$5(BintrayRepo.scala:186)
ThisBuild / releaseEarlyEnableSyncToMaven := false

ThisBuild / releaseEarlyWith := BintrayPublisher
ThisBuild / bintrayOrganization := Some("tersesystems")

ThisBuild / developers := List(
  Developer("wsargent", "Will Sargent", "will@tersesystems.com", url("https://tersesystems.com"))
)
ThisBuild / organization := "com.tersesystems.blindsight"
ThisBuild / organizationName := "Terse Systems"
ThisBuild / homepage := Some(url("https://tersesystems.github.io/blindsight"))

ThisBuild / startYear := Some(2020)
ThisBuild / licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / headerLicense := None

val disableDocs = Seq[Setting[_]](
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

val disablePublishing = Seq[Setting[_]](
  publishArtifact := false,
  skip in publish := true
)

// sbt ghpagesPushSite to publish to ghpages
// previewAuto to see the site in action.
// https://www.scala-sbt.org/sbt-site/getting-started.html#previewing-the-site
lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin, ParadoxSitePlugin, GhpagesPlugin, ScalaUnidocPlugin)
  .settings(
    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    libraryDependencies += cronScheduler ,
    libraryDependencies += scalaJava8Compat,
    libraryDependencies += logbackTracing % Test,
    libraryDependencies += logbackUniqueId % Test,
    libraryDependencies += logbackTypesafeConfig % Test,
    libraryDependencies += logbackExceptionMapping % Test,
    libraryDependencies += logbackExceptionMappingProvider % Test,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/tersesystems/blindsight"),
        "scm:git:git@github.com:tersesystems/blindsight.git"
      )
    ),
    git.remoteRepo := scmInfo.value.get.connection.replace("scm:git:", ""),
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    mappings in makeSite ++= Seq(
      file("LICENSE") -> "LICENSE"
    ),
    paradoxProperties in Compile ++= Map(
      "github.base_url"    -> s"https://github.com/tersesystems/blindsight/tree/v${version.value}",
      "canonical.base_url" -> "/blindsight/",
      "scaladoc.base_url"  -> "/blindsight/api/"
    ),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(fixtures),
    siteSubdirName in ScalaUnidoc := "api",
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc)
  )
  .settings(disablePublishing)
  .dependsOn(api, logstash)

lazy val fixtures = (project in file("fixtures"))
  .settings(
    libraryDependencies += scalaJava8Compat       % Test,
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    libraryDependencies += scalaTest              % Test
  )
  .settings(disablePublishing)
  .settings(disableDocs)

// API that provides a logger with everything
lazy val api = (project in file("api"))
  .settings(
    name := "blindsight-api",
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    libraryDependencies += scalaTest              % Test,
    libraryDependencies += slf4jApi,
    libraryDependencies += sourcecode,
    libraryDependencies += scalaJava8Compat       % Test,
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    libraryDependencies += scalaTest              % Test,
    autoAPIMappings := true
  )
  .dependsOn(fixtures % "test->test")

lazy val logstash = (project in file("logstash"))
  .settings(
    name := "blindsight-logstash",
    libraryDependencies += logbackClassic,
    libraryDependencies += logstashLogbackEncoder,
    autoAPIMappings := true
  )
  .dependsOn(api, fixtures % "test->test")

// serviceloader implementation with only SLF4J dependencies.
lazy val generic = (project in file("generic"))
  .settings(
    name := "blindsight-generic"
  )
  .dependsOn(api)

lazy val root = (project in file("."))
  .settings(
    name := "blindsight-root"
  )
  .settings(disableDocs)
  .settings(disablePublishing)
  .aggregate(api, docs, fixtures, logstash, generic)
