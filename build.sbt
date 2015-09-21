name               := "The Impatient Book"
scalaVersion       := Config.ver.scala.lang
crossScalaVersions := Seq(Config.ver.scala.lang)

/* TODO: Modify this template of sub-projects */
lazy val chapter13 = project.settings(Config.commonScalaSettings)
lazy val chapter14 = project.settings(Config.commonScalaSettings)
lazy val chapter15 = project.settings(Config.commonScalaSettings)
    .settings(libraryDependencies ++= Seq(javaJpa))

/* The Root Project */
lazy val root = (project in file(".")).
    enablePlugins(PlayJava).
    settings(EclipseKeys.skipProject := true).
    settings(Config.commonSettings).
    settings(Config.unidocSettings).
    settings(giter8.ScaffoldPlugin.scaffoldSettings:_*).
    aggregate(chapter13, chapter14, chapter15)

fork in run  := true
fork in test := true
