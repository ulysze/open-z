val scala3Version = "3.6.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "z",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.15",
      "dev.zio" %% "zio-test"          % "2.1.15" % Test,
      "dev.zio" %% "zio-test-sbt"      % "2.1.15" % Test,
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"
    )
  )
