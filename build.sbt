
import BuildSettings.Versions

organization := """"""
name := """finatra-example-seed"""
version := (Seq(milestone) ++ feature ++ snapshot).mkString("-").replaceAll("/", "-")

val milestone = "1.0.0"
val feature = None
val snapshot = Some("SNAPSHOT")
val _version = (Seq(milestone) ++ snapshot).mkString("-").replaceAll("/", "-")

scalaVersion := "2.12.4"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps"
)
scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 12)) => Seq("-Xlint:-unused")
  case _             => Seq("-Xlint")
})
scalacOptions in(Compile, doc) := Seq("-diagrams")

sources in(Compile, doc) := Seq.empty

libraryDependencies ++= Seq(
  "com.twitter"                %% "finatra-http"         % Versions.finatra,
  "com.twitter"                %% "finatra-httpclient"   % Versions.finatra,
  "com.typesafe"               %  "config"               % Versions.typesafeConfig,
  "com.github.cb372"           %% "scalacache-core"      % Versions.scalacache,
  "com.github.cb372"           %% "scalacache-guava"     % Versions.scalacache,
  "com.softwaremill.quicklens" %% "quicklens"            % Versions.quicklens,
  "ch.qos.logback"             %  "logback-classic"      % Versions.logback,
  "org.scalaz"                 %% "scalaz-core"          % Versions.scalazCore
)

libraryDependencies ++= Seq(
  "com.twitter"                %% "finagle-redis"        % Versions.finatra,
  "org.mongodb.scala"          %% "mongo-scala-driver"   % Versions.mongodb,
  "io.getquill"                %% "quill-async-mysql"    % Versions.quillAsyncMysql,
  "redis.clients"              %  "jedis"                % Versions.jedis,
  "net.debasishg"              %% "redisclient"          % Versions.redisclient
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-actor"           % Versions.akkaVersion,
  "com.typesafe.akka"          %% "akka-stream"          % Versions.akkaVersion,
  "com.typesafe.akka"          %% "akka-slf4j"           % Versions.akkaVersion,
  "com.typesafe.akka"          %% "akka-stream-testkit"  % Versions.akkaVersion % Test
)

libraryDependencies ++= Seq(
  "com.google.inject.extensions" % "guice-testlib" % Versions.guice % "test",

  "com.twitter"                %% "finatra-http"         % Versions.finatra   % "test" classifier "tests",
  "com.twitter"                %% "finatra-jackson"      % Versions.finatra   % "test" classifier "tests",
  "com.twitter"                %% "inject-server"        % Versions.finatra   % "test" classifier "tests",
  "com.twitter"                %% "inject-app"           % Versions.finatra   % "test" classifier "tests",
  "com.twitter"                %% "inject-core"          % Versions.finatra   % "test" classifier "tests",
  "com.twitter"                %% "inject-modules"       % Versions.finatra   % "test" classifier "tests"
)

libraryDependencies ++= Seq(
  "org.mockito"                %  "mockito-core"         % Versions.mockito    % "test",
  "org.scalacheck"             %% "scalacheck"           % Versions.scalaCheck % "test",
  "org.scalatest"              %% "scalatest"            % Versions.scalaTest  % "test",
  "org.specs2"                 %% "specs2-core"          % Versions.specs2     % "test",
  "org.specs2"                 %% "specs2-junit"         % Versions.specs2     % "test",
  "org.specs2"                 %% "specs2-mock"          % Versions.specs2     % "test"
)


dependencyOverrides ++= Set(
  "com.google.code.findbugs"   %  "jsr305"               % Versions.findbugs,
  "com.google.guava"           %  "guava"                % Versions.guava
)
