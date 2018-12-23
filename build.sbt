name := "datahub"

version := "0.1"

scalaVersion := "2.12.4"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Classpaths.sbtPluginReleases
)

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.specs" % "specs" % "1.4.3" % "test",
    "org.mockito" % "mockito-all" % "1.10.19" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
  )
)

lazy val root = (project in file(".")).aggregate(core, examples, datahubJava)

lazy val core = (project in file("core")).settings(commonSettings)

lazy val examples = (project in file("examples"))
  .dependsOn(core)
  .settings(commonSettings)

lazy val datahubJava = project in file("datahub-java")