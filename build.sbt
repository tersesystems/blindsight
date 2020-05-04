import Dependencies._
import sbt.Keys.libraryDependencies

lazy val scala213 = "2.13.1"
lazy val scala212 = "2.12.11"
lazy val scala211 = "2.11.12"
ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212, scala213)

ThisBuild / scalafmtOnCompile := true

ThisBuild / pgpPublicRing := file(".travis/local.pubring.asc")
ThisBuild / pgpSecretRing := file(".travis/local.secring.asc")
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
    libraryDependencies += logbackTracing,
    libraryDependencies += logbackUniqueId,
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1",
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
  .dependsOn(all, logstash)

lazy val fixtures = (project in file("fixtures"))
  .settings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test
  )
  .settings(disablePublishing)
  .settings(disableDocs)

lazy val api = (project in file("api")).settings(
  name := "blindsight-api",
  libraryDependencies += slf4jApi,
  libraryDependencies += sourcecode,
  autoAPIMappings := true
)

lazy val slf4j = (project in file("slf4j"))
  .settings(
    name := "blindsight-slf4j",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test,
    autoAPIMappings := true
  )
  .dependsOn(api, fixtures % "test->test")

lazy val semantic = (project in file("semantic"))
  .settings(
    name := "blindsight-semantic",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += scalaTest                % Test,
    autoAPIMappings := true
  )
  .dependsOn(slf4j, api)
  .dependsOn(fixtures % "test->test")

lazy val flow = (project in file("flow"))
  .settings(
    name := "blindsight-flow",
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    libraryDependencies += scalaTest              % Test,
    autoAPIMappings := true
  )
  .dependsOn(slf4j, fixtures % "test->test")

lazy val fluent = (project in file("fluent"))
  .settings(
    name := "blindsight-fluent",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1" % Test,
    libraryDependencies += logbackClassic           % Test,
    libraryDependencies += logstashLogbackEncoder   % Test,
    libraryDependencies += scalaTest                % Test,
    autoAPIMappings := true
  )
  .dependsOn(slf4j, api)
  .dependsOn(fixtures % "test->test")

// API that provides a logger with everything
lazy val all = (project in file("all"))
  .settings(
    name := "blindsight"
  )
  .dependsOn(api, slf4j, semantic, fluent, flow)

lazy val logstash = (project in file("logstash"))
  .settings(
    name := "blindsight-logstash",
    libraryDependencies += logbackClassic,
    libraryDependencies += jacksonModuleScala,
    libraryDependencies += logstashLogbackEncoder,
    autoAPIMappings := true
  )
  .dependsOn(all, fixtures % "test->test")

// serviceloader implementation with only SLF4J dependencies.
lazy val generic = (project in file("generic"))
  .settings(
    name := "blindsight-generic"
  )
  .dependsOn(all)

lazy val root = (project in file("."))
  .settings(
    name := "blindsight-root"
  )
  .settings(disableDocs)
  .settings(disablePublishing)
  .aggregate(docs, fixtures, api, slf4j, semantic, fluent, flow, logstash, all, generic)
