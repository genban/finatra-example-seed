import sbt._
import Keys._

object BuildSettings {

  val settings: Seq[Setting[_]] = Seq(
    organization := """""",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.jcenterRepo,
      Resolvers.TwitterMaven,
      Resolvers.FinatraRepo,
      Resolvers.ScalazBintray,
      Resolvers.LocalMaven
    )
  )

  val scalacOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:experimental.macros",
    "-unchecked",
    "-Ywarn-nullary-unit",
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Xfuture"
  )

  object Versions {

    val akkaVersion       = "2.4.18"
    val finatra           = "17.12.0"
    val finatraHttp       = "17.12.0"
    val finagleRedis      = "7.1.0"
    val finatraSlf4j      = "2.13.0"
    val commonsCodec      = "1.9"
    val commonsFileupload = "1.3.1"
    val commonsIo         = "2.4"
    val commonsLang       = "2.6"
    val guava             = "19.0"
    val guice             = "4.0"
    val ficus             = "1.4.0"
    val findbugs          = "2.0.1"
    val jackson           = "2.8.4"
    val jedis             = "2.9.0"
    val jodaConvert       = "1.2"
    val jodaTime          = "2.5"
    val junit             = "4.12"
    val libThrift         = "0.5.0-7"
    val logback           = "1.1.7"
    val mockito           = "1.9.5"
    val mongodb           = "2.1.0"
    val mustache          = "0.8.18"
    val mysql             = "5.1.36"
    val nscalaTime        = "2.14.0"
    val redisclient       = "3.4"
    val scalacache        = "0.21.0"
    val scalaCheck        = "1.13.4"
    val scalaGuice        = "4.1.0"
    val scalaTest         = "3.0.0"
    val scalaj            = "2.3.0"
    val scalazCore        = "7.2.16"
    val slf4j             = "1.7.21"
    val snakeyaml         = "1.12"
    val specs2            = "2.4.17"
    val quicklens         = "1.4.11"
    val quillAsyncMysql   = "2.2.0"
    val quillFinagleMysql = "2.2.0"
    val typesafeConfig    = "1.3.1"
  }

  object Resolvers {

    val TwitterMaven        = "Twitter Maven" at "https://maven.twttr.com"
    val FinatraRepo         = "Finatra Repo" at "http://twitter.github.com/finatra"
    val ScalazBintray       = "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
    val Snapshots           = "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
    val SonatypeReleases    = "releases" at "http://oss.sonatype.org/content/repositories/releases"
    val SonatypeOSSReleases = "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    val LocalMaven          = "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.ivy2"
    val MavenMirrorid       = "maven.mirrorid" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/"
    val TypesafeSnapshots   = "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
    val TypesafeRepository  = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  }
}
