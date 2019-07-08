name := """concerts"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json-joda" % "2.6.0",
  "com.typesafe.play" %% "play-jdbc-api" % "2.7.3",
  "com.typesafe.play" %% "play-ws" % "2.7.3",
  guice
)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += ws
libraryDependencies += ehcache

libraryDependencies += jdbc

// https://mvnrepository.com/artifact/org.playframework.anorm/anorm
libraryDependencies ++= Seq("com.typesafe.play" %% "anorm" % "2.5.0")

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.6"
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
