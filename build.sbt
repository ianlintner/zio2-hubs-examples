ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.lintner.ian"
ThisBuild / organizationName := "Ian Lintner"

lazy val root = (project in file("."))
  .settings(
    name := "hubs",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-streams" % "2.0.9",
      "dev.zio" %% "zio-logging" % "2.1.10",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.10",
      "ch.qos.logback" % "logback-classic" % "1.4.5"
    )
  )
