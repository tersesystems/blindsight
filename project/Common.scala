import Dependencies._
import de.heikoseeberger.sbtheader.HeaderPlugin
import sbt.Keys._
import sbt._

// https://www.scala-sbt.org/1.x/docs/Plugins.html#Creating+an+auto+plugin
object Common extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin && HeaderPlugin
  override def trigger           = allRequirements

  override def globalSettings: Seq[Def.Setting[_]] = {
    Seq(
      libraryDependencies += logbackClassic           % Test,
      libraryDependencies += logstashLogbackEncoder   % Test,
      libraryDependencies += scalaTest                % Test
    )
  }
}
