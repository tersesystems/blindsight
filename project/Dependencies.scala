import sbt.Keys.{scalaOrganization, scalaVersion}
import sbt._

object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15"

  val terseLogback = "1.2.0"

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.36"

  lazy val jctools = "org.jctools" % "jctools-core" % "4.0.1"

  // import scala.jdk.CollectionConverters._
  // https://github.com/scala/scala-library-compat/pull/217
  lazy val scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.9.0"
  lazy val sourcecode            = "com.lihaoyi"            %% "sourcecode"              % "0.3.0"
  lazy val janino                = "org.codehaus.janino"     % "janino"                  % "3.0.11"
  lazy val jansi                 = "org.fusesource.jansi"    % "jansi"                   % "1.17.1"

  lazy val logbackBudget      = "com.tersesystems.logback" % "logback-budget"      % terseLogback
  lazy val logbackTurboMarker = "com.tersesystems.logback" % "logback-turbomarker" % terseLogback
  lazy val logbackTypesafeConfig =
    "com.tersesystems.logback" % "logback-typesafe-config" % terseLogback
  lazy val logbackExceptionMapping =
    "com.tersesystems.logback" % "logback-exception-mapping" % terseLogback
  lazy val logbackExceptionMappingProvider =
    "com.tersesystems.logback" % "logback-exception-mapping-providers" % terseLogback

  def refined(scalaVersion: String): ModuleID =
    scalaVersion match {
      case s if s.startsWith("2.11") => "eu.timepit" %% "refined" % "0.9.12"
      case _                         => "eu.timepit" %% "refined" % "0.10.2"
    }

  lazy val logbackUniqueId = "com.tersesystems.logback" % "logback-uniqueid-appender" % terseLogback
  lazy val logbackTracing  = "com.tersesystems.logback" % "logback-tracing"           % terseLogback
  lazy val logbackClassic  = "ch.qos.logback"           % "logback-classic"           % "1.2.12"
  lazy val logstashLogbackEncoder = "net.logstash.logback"    % "logstash-logback-encoder" % "7.3"
  lazy val cronScheduler          = "io.timeandspace"         % "cron-scheduler"           % "0.1"
  lazy val scalaJava8Compat       = "org.scala-lang.modules" %% "scala-java8-compat"       % "1.0.2"
  lazy val tweakFlow              = "com.twineworks"          % "tweakflow"                % "1.4.3"
  lazy val securitybuilder = "com.tersesystems.securitybuilder" % "securitybuilder" % "1.0.1"
}
