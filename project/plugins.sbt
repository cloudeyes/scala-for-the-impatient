resolvers ++= Seq(
  "simplytyped" at "http://simplytyped.github.io/repo/releases",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "spray repo" at "http://repo.spray.io"
)

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.3")
addSbtPlugin("net.databinder.giter8" % "giter8-scaffold" % "0.6.9")
//addSbtPlugin("net.ceedubs" %% "sbt-ctags" % "0.1.0")
//addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.7.6")
//addSbtPlugin("io.spray"     % "sbt-revolver" % "0.7.2")
