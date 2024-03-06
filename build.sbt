import Dependencies._
import sbt.Keys._
import commandmatrix._

initialize := {
  val _        = initialize.value // run the previous initialization
  val required = "11"
  val current  = sys.props("java.specification.version")
  assert(current >= required, s"Unsupported JDK: java.specification.version $current != $required")
}

val scala3        = "3.3.3"
val scala213      = "2.13.10"
val scala212      = "2.12.17"
val scala211      = "2.11.12"
val scalaVersions = Seq(scala3, scala213, scala212, scala211)

inThisBuild(
  Seq(
    // sbt-commandmatrix
    commands ++= CrossCommand.single(
      "test",
      matrices = Seq(root),
      dimensions = Seq(
        Dimension.scala("2.13", fullFor3 = true),
        Dimension.platform()
      )
    )
  )
)

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scalafmtOnCompile := false

ThisBuild / description := "Rich Typesafe Scala Logging API based on SLF4J"

ThisBuild / organization := "com.tersesystems.blindsight"

ThisBuild / homepage := Some(url("https://tersesystems.github.io/blindsight"))

ThisBuild / startYear := Some(2020)
ThisBuild / licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/tersesystems/blindsight"),
    "scm:git@github.com:tersesystems/blindsight.git"
  )
)

val disableDocs = Seq[Setting[_]](
  Compile / doc / sources                := Seq.empty,
  Compile / packageDoc / publishArtifact := false
)

val disablePublishing = Seq[Setting[_]](
  publishArtifact := false,
  publish / skip  := true
)

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  (CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) =>
      Seq(
        "-language:implicitConversions",
        "-release",
        "8"
      )
    case Some((2, n)) if n >= 13 =>
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
      ) ++
        Seq(
          "-Xsource:2.13",
          "-Xfatal-warnings",
          "-Wconf:any:warning-verbose",
          "-release",
          "8"
        ) ++ optimizeInline
    case Some((2, n)) if n == 12 =>
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
      ) ++ Seq(
        "-Xsource:2.12",
        "-Yno-adapted-args"
        // "-release", "8" https://github.com/scala/bug/issues/11927 scaladoc is busted in 2.11.11
        // "-Xfatal-warnings" https://github.com/scala/bug/issues/7707 still broken in 2.12
      ) ++ optimizeInline
    case Some((2, n)) if n == 11 =>
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
      ) ++ Seq(
        "-Xsource:2.11",
        "-Yno-adapted-args",
        "-Xfatal-warnings"
      )
  })
}

// API that provides a logger with everything
lazy val api = (projectMatrix in file("api"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight"))
  .settings(
    name := "blindsight-api",
    //    mimaPreviousArtifacts := Set(
    //      "com.tersesystems.blindsight" %% moduleName.value % previousVersion
    //    ),
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += slf4jApi,
    libraryDependencies += sourcecode,
    libraryDependencies += scalaCollectionCompat, // should not be in 2.13 or 3.0
    // scala-reflect only needed for Statement Interpolation
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq.empty
        case other =>
          Seq(
            "org.scala-lang" % "scala-reflect" % scalaVersion.value
          )
      }
    },
    libraryDependencies += scalaTest              % Test,
    libraryDependencies += scalaJava8Compat       % Test,
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(fixtures % "test->test" /* tests in api depend on test code in fixtures */ )

lazy val dsl = (projectMatrix in file("dsl"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.dsl"))
  .settings(
    name          := "blindsight-dsl",
    scalacOptions := scalacOptionsVersion(scalaVersion.value)
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api)

lazy val ringbuffer = (projectMatrix in file("ringbuffer"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.ringbuffer"))
  .settings(
    name          := "blindsight-ringbuffer",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += jctools
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api)

lazy val jsonld = (projectMatrix in file("jsonld"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.jsonld"))
  .settings(
    name                            := "blindsight-jsonld",
    libraryDependencies += scalaTest % Test,
    scalacOptions                   := scalacOptionsVersion(scalaVersion.value)
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api, dsl)

lazy val logstash = (projectMatrix in file("logstash"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.logstash"))
  .settings(
    name          := "blindsight-logstash",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += logbackClassic,
    libraryDependencies += logstashLogbackEncoder
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api, dsl, fixtures % "test->test")

lazy val inspections = (projectMatrix in file("inspections"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.inspection"))
  .settings(
    name := "blindsight-inspection",
    libraryDependencies ++= {
      // Compile / scalafmtConfig := file(".scalafmt-dotty.conf")
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq.empty
        case _ =>
          Seq(
            "org.scala-lang" % "scala-reflect" % scalaVersion.value
          )
      }
    },
    scalacOptions := scalacOptionsVersion(scalaVersion.value)
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api % Test, fixtures % "test->test")

lazy val scripting = (projectMatrix in file("scripting"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.scripting"))
  .settings(
    name          := "blindsight-scripting",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += tweakFlow,
    libraryDependencies += securitybuilder % Test
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api, fixtures % "test->test")

// https://github.com/ktoso/sbt-jmh
lazy val benchmarks = (projectMatrix in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    run / fork := true
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .settings(disableDocs)
  .settings(disablePublishing)
  .dependsOn(logstash, ringbuffer)

// serviceloader implementation with only SLF4J dependencies.
lazy val generic = (projectMatrix in file("generic"))
  .settings(AutomaticModuleName.settings("com.tersesystems.blindsight.generic"))
  .settings(
    name          := "blindsight-generic",
    scalacOptions := scalacOptionsVersion(scalaVersion.value)
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(api)

// sbt ghpagesPushSite to publish to ghpages
// previewAuto to see the site in action.
// https://www.scala-sbt.org/sbt-site/getting-started.html#previewing-the-site
lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxPlugin, ParadoxSitePlugin, GhpagesPlugin, ScalaUnidocPlugin)
  .settings(
    scalaVersion                                          := scala213,
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
    paradoxTheme   := Some(builtinParadoxTheme("generic")),
    makeSite / mappings ++= Seq(
      file("LICENSE") -> "LICENSE"
    ),
    Compile / paradoxProperties ++= Map(
      "github.base_url"    -> s"https://github.com/tersesystems/blindsight/tree/v${version.value}",
      "canonical.base_url" -> "/blindsight/",
      "scaladoc.base_url"  -> "/blindsight/api/"
    ),
    (ScalaUnidoc / unidoc) / unidocProjectFilter := inAnyProject -- inProjects(
      fixtures.jvm(scala213)
    ),
    ScalaUnidoc / siteSubdirName := "api",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, ScalaUnidoc / siteSubdirName)
  )
  .settings(disablePublishing)
  .dependsOn(
    api.jvm(scala213),
    logstash.jvm(scala213),
    jsonld.jvm(scala213),
    ringbuffer.jvm(scala213),
    scripting.jvm(scala213)
  )

lazy val fixtures = (projectMatrix in file("fixtures"))
  .settings(
    libraryDependencies += scalaJava8Compat       % Test,
    libraryDependencies += logbackClassic         % Test,
    libraryDependencies += logstashLogbackEncoder % Test,
    libraryDependencies += scalaTest              % Test,
    Test / compileOrder                          := CompileOrder.JavaThenScala
  )
  .jvmPlatform(scalaVersions = scalaVersions)
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
  // Error:(51, 53) com/tersesystems/blindsight/LoggerFactory$::getLogger(Lscala/Function0;Lcom/tersesystems/blindsight/LoggerResolver;)Lcom/tersesystems/blindsight/Logger; could not be inlined:
  // The callee com/tersesystems/blindsight/LoggerFactory$::getLogger(Lscala/Function0;Lcom/tersesystems/blindsight/LoggerResolver;)Lcom/tersesystems/blindsight/Logger; contains the instruction INVOKESPECIAL com/tersesystems/blindsight/LoggerFactory$.loggerFactory ()Lcom/tersesystems/blindsight/LoggerFactory;
  // that would cause an IllegalAccessError when inlined into class com/tersesystems/blindsight/logstash/LogstashLoggerSpec.
  // val logger: Logger = LoggerFactory.getLogger(underlying)
  // "-opt-warnings:any-inline-failed"
)

lazy val root = (projectMatrix in file("."))
  .settings(
    name := "blindsight-root"
  )
  .settings(disableDocs)
  .settings(disablePublishing)
  .aggregate(
    api,
    dsl,
    fixtures,
    inspections,
    scripting,
    ringbuffer,
    jsonld,
    logstash,
    generic
  )
  .jvmPlatform(scalaVersions = scalaVersions)
