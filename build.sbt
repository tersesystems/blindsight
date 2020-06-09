import Dependencies._
import sbt.Keys.libraryDependencies

// Sanity check for sbt-travisci
Global / onLoad := (Global / onLoad).value.andThen { s =>
  val v = scala213.value
  if (!CrossVersion.isScalaApiCompatible(v))
    throw new MessageOnlyException(
      s"Key scala213 doesn't define a scala version. Check .travis.yml is setup right. Version: $v"
    )
  s
}

ThisBuild / scalafmtOnCompile := false

// These settings seem not to work for sbt-release-early, so .travis.yml copies to
// .sbt/gpg/pubring.asc / secring.asc as a fallback
ThisBuild / pgpPublicRing := file(".travis/local.pubring.asc")
ThisBuild / pgpSecretRing := file(".travis/local.secring.asc")

// https://github.com/jvican/sbt-release-early/wiki/How-to-release-with-Bintray#releasing-to-maven-central
// Disable sync to maven, we absolutely don't need this in sbt
// and it causes a None.get error at bintray.BintrayRepo.$anonfun$requestSonatypeCredentials$5(BintrayRepo.scala:186)
ThisBuild / releaseEarlyEnableSyncToMaven := false

ThisBuild / resolvers += Resolver.bintrayRepo("tersesystems", "maven")

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

val previousVersion = "1.1.0"

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
  .disablePlugins(MimaPlugin)
  .settings(
    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    libraryDependencies += cronScheduler                   % Test,
    libraryDependencies += scalaJava8Compat                % Test,
    libraryDependencies += logbackTracing                  % Test,
    libraryDependencies += refined(scalaVersion.value)     % Test,
    libraryDependencies += logbackUniqueId                 % Test,
    libraryDependencies += logbackTypesafeConfig           % Test,
    libraryDependencies += logbackExceptionMapping         % Test,
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
  .disablePlugins(MimaPlugin)
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
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight"))
  .settings(
    name := "blindsight-api",
    mimaPreviousArtifacts := Set(
      "com.tersesystems.blindsight" %% moduleName.value % previousVersion
    ),
    libraryDependencies += slf4jApi,
    libraryDependencies += sourcecode,
    libraryDependencies += scalaTest              % Test,
    libraryDependencies += scalaJava8Compat       % Test,
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    autoAPIMappings := true
  )
  .dependsOn(fixtures % "test->test" /* tests in api depend on test code in fixtures */ )

lazy val logstash = (project in file("logstash"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.logstash"))
  .settings(
    name := "blindsight-logstash",
    mimaPreviousArtifacts := Set(
      "com.tersesystems.blindsight" %% moduleName.value % previousVersion
    ),
    libraryDependencies += logbackClassic,
    libraryDependencies += logstashLogbackEncoder,
    autoAPIMappings := true
  )
  .dependsOn(api, fixtures % "test->test")

// https://github.com/ktoso/sbt-jmh
// http://tutorials.jenkov.com/java-performance/jmh.html
// https://www.researchgate.net/publication/333825812_What's_Wrong_With_My_Benchmark_Results_Studying_Bad_Practices_in_JMH_Benchmarks
// run with "jmh:run"
lazy val benchmarks = (project in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    libraryDependencies += "com.portingle" % "slf4jtesting" % "1.1.3",
    fork in run := true
  )
  .dependsOn(api)

// serviceloader implementation with only SLF4J dependencies.
lazy val generic = (project in file("generic"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.generic"))
  .settings(
    name := "blindsight-generic",
    mimaPreviousArtifacts := Set("com.tersesystems.blindsight" %% moduleName.value % "1.1.0")
  )
  .dependsOn(api)

lazy val root = (project in file("."))
  .disablePlugins(MimaPlugin)
  .settings(
    name := "blindsight-root"
  )
  .settings(disableDocs)
  .settings(disablePublishing)
  .aggregate(api, docs, fixtures, benchmarks, logstash, generic)
