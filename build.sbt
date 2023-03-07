ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.pgvector"
ThisBuild / organizationName := "pgvector"

lazy val root = (project in file("."))
  .settings(
    name := "pgvector",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.5.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.typesafe.slick" %% "slick" % "3.4.1" % Test,
      "org.slf4j" % "slf4j-nop" % "1.7.26" % Test
    )
  )
