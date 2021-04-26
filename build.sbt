import Dependencies._
import com.typesafe.tools.mima.core.{
  DirectMissingMethodProblem,
  IncompatibleMethTypeProblem,
  IncompatibleResultTypeProblem,
  IncompatibleTemplateDefProblem,
  InheritedNewAbstractMethodProblem,
  MissingClassProblem,
  MissingTypesProblem,
  ProblemFilters,
  ReversedMissingMethodProblem
}
import sbt.Keys.libraryDependencies

initialize := {
  val _        = initialize.value // run the previous initialization
  val required = "11"
  val current  = sys.props("java.specification.version")
  assert(current >= required, s"Unsupported JDK: java.specification.version $current != $required")
}

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

ThisBuild / releaseEarlyWith := SonatypePublisher

ThisBuild / developers := List(
  Developer("wsargent", "Will Sargent", "will@tersesystems.com", url("https://tersesystems.com"))
)
ThisBuild / organization := "com.tersesystems.blindsight"
ThisBuild / organizationName := "Terse Systems"
ThisBuild / homepage := Some(url("https://tersesystems.github.io/blindsight"))

ThisBuild / startYear := Some(2020)
ThisBuild / licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / headerLicense := None

val previousVersion = "1.3.0"

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
  .dependsOn(api, logstash, jsonld, ringbuffer)

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

// inliner must be run with "clean; compile", it's not incremental
// https://www.lightbend.com/blog/scala-inliner-optimizer
// https://docs.scala-lang.org/overviews/compiler-options/index.html
val optimizeInline = Seq(
  "-opt:l:inline",
  "-opt-inline-from:com.tersesystems.blindsight.**",
  "-opt-warnings:none"
  // have to comment this out as it fails on this:
  //Error:(51, 53) com/tersesystems/blindsight/LoggerFactory$::getLogger(Lscala/Function0;Lcom/tersesystems/blindsight/LoggerResolver;)Lcom/tersesystems/blindsight/Logger; could not be inlined:
  //The callee com/tersesystems/blindsight/LoggerFactory$::getLogger(Lscala/Function0;Lcom/tersesystems/blindsight/LoggerResolver;)Lcom/tersesystems/blindsight/Logger; contains the instruction INVOKESPECIAL com/tersesystems/blindsight/LoggerFactory$.loggerFactory ()Lcom/tersesystems/blindsight/LoggerFactory;
  //that would cause an IllegalAccessError when inlined into class com/tersesystems/blindsight/logstash/LogstashLoggerSpec.
  //val logger: Logger = LoggerFactory.getLogger(underlying)
  //"-opt-warnings:any-inline-failed"
)

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-encoding",
    "UTF-8",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Yrangepos"
  ) ++ (CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n >= 13 =>
      Seq(
        "-Xsource:2.13",
        "-Xfatal-warnings",
        "-Wconf:any:warning-verbose",
        "-release",
        "8"
      ) ++ optimizeInline
    case Some((2, n)) if n == 12 =>
      Seq(
        "-Xsource:2.12",
        "-Yno-adapted-args"
        // "-release", "8" https://github.com/scala/bug/issues/11927 scaladoc is busted in 2.11.11
        // "-Xfatal-warnings" https://github.com/scala/bug/issues/7707 still broken in 2.12
      ) ++ optimizeInline
    case Some((2, n)) if n == 11 =>
      Seq(
        "-Xsource:2.11",
        "-Yno-adapted-args",
        "-Xfatal-warnings"
      )
  })
}

// API that provides a logger with everything
lazy val api = (project in file("api"))
  .disablePlugins(MimaPlugin)
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight"))
  .settings(
    name := "blindsight-api",
    //    mimaPreviousArtifacts := Set(
    //      "com.tersesystems.blindsight" %% moduleName.value % previousVersion
    //    ),
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += slf4jApi,
    libraryDependencies += sourcecode,
    libraryDependencies += scalaCollectionCompat,
    libraryDependencies += scalaTest              % Test,
    libraryDependencies += scalaJava8Compat       % Test,
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    autoAPIMappings := true
  )
  .dependsOn(fixtures % "test->test" /* tests in api depend on test code in fixtures */ )

lazy val ringbuffer = (project in file("ringbuffer"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.ringbuffer"))
  .settings(
    name := "blindsight-ringbuffer",
    mimaPreviousArtifacts := Set.empty,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += "org.jctools" % "jctools-core" % "3.2.0",
    autoAPIMappings := true
  )
  .dependsOn(api)

lazy val jsonld = (project in file("jsonld"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.jsonld"))
  .settings(
    name := "blindsight-jsonld",
    mimaPreviousArtifacts := Set.empty,
    libraryDependencies += scalaTest % Test,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    autoAPIMappings := true
  )
  .dependsOn(api)

lazy val logstash = (project in file("logstash"))
  .disablePlugins(MimaPlugin)
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.logstash"))
  .settings(
    name := "blindsight-logstash",
    //    mimaPreviousArtifacts := Set(
    //      "com.tersesystems.blindsight" %% moduleName.value % previousVersion
    //    ),
    //    mimaBinaryIssueFilters := Seq(
    //      ProblemFilters.exclude[IncompatibleResultTypeProblem](
    //        "com.tersesystems.blindsight.logstash.LogstashArgumentResolver.resolve"
    //      )
    //    ),
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
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
  .disablePlugins(MimaPlugin)
  .settings(
    fork in run := true
  )
  .settings(disableDocs)
  .settings(disablePublishing)
  .dependsOn(logstash, ringbuffer)

// serviceloader implementation with only SLF4J dependencies.
lazy val generic = (project in file("generic"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.generic"))
  .settings(
    name := "blindsight-generic",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
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
  .aggregate(api, docs, fixtures, benchmarks, logstash, ringbuffer, jsonld, generic)
