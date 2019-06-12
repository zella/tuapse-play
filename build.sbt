ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "org.zella"
ThisBuild / organizationName := "zella"

lazy val root = (project in file("."))
  .settings(name := "tuapse-play")

assemblyMergeStrategy in assembly := {
    case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
    case x if x.contains("libjnidispatch.so") => MergeStrategy.last
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}
test in assembly := {}

assemblyOutputPath in assembly := file("build/assembly.jar")

// https://mvnrepository.com/artifact/io.reactivex.rxjava2/rxjava
libraryDependencies += "io.reactivex.rxjava2" % "rxjava" % "2.2.8"
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "com.github.zella" % "rx-process2" % "0.1.0-RC3"

libraryDependencies += "com.github.davidmoten" % "rxjava2-extras" % "0.1.+"

// https://mvnrepository.com/artifact/io.vavr/vavr
libraryDependencies += "io.vavr" % "vavr" % "0.10.0"
// https://mvnrepository.com/artifact/io.vertx/vertx-rx-java2
libraryDependencies += "io.vertx" % "vertx-rx-java2" % "3.7.1"
// https://mvnrepository.com/artifact/io.vertx/vertx-web
libraryDependencies += "io.vertx" % "vertx-web" % "3.7.1"
// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.6"

libraryDependencies += "com.typesafe" % "config" % "1.3.4"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0" % Test