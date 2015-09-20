import sbt._ 
import sbt.Keys._
import com.typesafe.sbteclipse.core.EclipsePlugin._
import EclipseBuild._
//import spray.revolver.RevolverPlugin.Revolver._

object Config {
  object ver {
    object scala {
      val lang    = "2.11.7"
      val test    = "2.2.5"
      val logging = "3.1.0"
    }
    val akka         = "2.3.12"
    val spray        = "1.3.2"
    val junit        = "4.12"
    object slf4j   { val api = "1.7.12" }
    object logback { val classic = "1.1.3" }
  }

  val junitLib     = "junit"    % "junit"       % ver.junit % "test"
  val sprayJsonLib = "io.spray" %% "spray-json" % ver.spray

  val commonLibs       = Seq(junitLib)
  val unidocSettings   = Unidoc.unidocSettings
  val scaladocSettings = Unidoc.scaladocSettings
  val scmInfo          = Some(ScmInfo(
    browseUrl = url("http://your.github.repo"), 
    connection = "scm:git:git@your.github.repo:account/project-name.git"))
  val scaladocDiagramsEnabled = true
  val scaladocAutoAPI = true 
  val genjavadocEnabled = true

  val commonSettings = Seq(
      logLevel        := Level.Warn,
      resolvers       ++= Seq(Resolver.mavenLocal),
      organization    := "cloudeyes.study.scala",
      version         := "0.0.1",
      autoAPIMappings := true,
      EclipseKeys.withSource := true,
      EclipseKeys.eclipseOutput := Some("target"),
      javacOptions ++= Seq("-encoding", "UTF-8"),
      libraryDependencies ++= commonLibs
  )

  val commonJavaSettings = commonSettings ++ Seq(
      crossPaths := false, // 아티팩트에 스칼라 버전을 붙이지 않는다.
      autoScalaLibrary := false,
      EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
  )

  val commonScalaLibs = Seq(
      "org.scalatest"  %% "scalatest"     % ver.scala.test % "test",
      "com.typesafe.scala-logging" %% "scala-logging" % ver.scala.logging,
      "ch.qos.logback" % "logback-classic" % ver.logback.classic ,
      "org.slf4j"      % "slf4j-api"       % ver.slf4j.api
  )

  val CommonAntlrSettings = commonJavaSettings ++ Seq(
      sourceManaged in Compile <<= baseDirectory { _ / "target/generated-sources/antlr4/parser" },
      EclipseKeys.classpathTransformerFactories := sampleClasspathTransformer1,
      EclipseKeys.projectTransformerFactories   := sampleProjectTransformer1((baseDirectory.value).getAbsolutePath)
  )

  val commonScalaSettings = commonSettings ++ scaladocSettings ++ Seq(
      scalaVersion  := ver.scala.lang,
      ivyScala      := ivyScala.value.map { _.copy(overrideScalaVersion = true) },
      scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature"),
      scalacOptions in (Compile, doc) ++= Seq(
        "-diagrams", "-diagrams-dot-path", "/usr/local/bin/dot"
        //"-doc-title", name.value
      ),
      libraryDependencies ++= commonScalaLibs
  )

  val webServerSettings = commonScalaSettings ++ Seq(
      resolvers           ++= Seq("spray repo" at "http://repo.spray.io"),
      unmanagedJars in Compile := 
          ((baseDirectory.value / "lib") ** "*.jar").classpath, 
      libraryDependencies ++= Seq(
      "io.spray"          %%  "spray-routing" % ver.spray,
      "io.spray"          %%  "spray-can"     % ver.spray,
      "io.spray"          %%  "spray-json"    % ver.spray,
      "com.typesafe.akka" %%  "akka-actor"    % ver.akka,
      "com.typesafe.akka" %%  "akka-slf4j"    % ver.akka
      //"io.spray"          %%  "spray-testkit" % ver.spray,
      //"com.typesafe.akka" %%  "akka-testkit"  % ver.akka,
      ),
      fork in Test := true,
      baseDirectory in run := file("src/main/resource/web"),
      javaOptions in Test += "-Djava.library.path=lib/" + ostype,
      watchSources := watchSources.value.filter { f => 
          val path = f.getPath
          val shouldExclude = 
            path.indexOf("/web/") >= 0 || path.indexOf(".tags") >= 0
          shouldExclude != true
      },
      EclipseKeys.classpathTransformerFactories := 
          sampleClasspathTransformer2((baseDirectory.value).getAbsolutePath),
      EclipseKeys.projectTransformerFactories := sampleProjectTransformer2
  )
}
