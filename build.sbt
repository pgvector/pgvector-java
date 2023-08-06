ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.3"
ThisBuild / versionScheme    := Some("early-semver")
ThisBuild / organization     := "com.pgvector"
ThisBuild / organizationName := "pgvector"
ThisBuild / organizationHomepage := Some(url("https://github.com/pgvector"))

lazy val root = (project in file("."))
  .settings(
    name := "pgvector",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.5.4",
      "org.scala-lang" % "scala-library" % scalaVersion.value % Test,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.typesafe.slick" %% "slick" % "3.4.1" % Test,
      "org.slf4j" % "slf4j-nop" % "1.7.26" % Test,
      "org.springframework" % "spring-jdbc" % "5.3.27" % Test,
      "org.hibernate" % "hibernate-core" % "5.6.15.Final" % Test
    ),
    crossPaths := false,
    autoScalaLibrary := false
  )

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/pgvector/pgvector-java"),
    "scm:git:https://github.com/pgvector/pgvector-java.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "ankane",
    name = "Andrew Kane",
    email = "andrew@ankane.org",
    url = url("https://github.com/ankane")
  )
)

ThisBuild / description := "pgvector support for Java and Scala"
ThisBuild / licenses := List(
  "MIT" -> url("https://opensource.org/license/mit/")
)
ThisBuild / homepage := Some(url("https://github.com/pgvector/pgvector-java"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
