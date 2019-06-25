import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val commonSettings = Seq(

  name := "b-tree",

  version := "0.6.1",

  scalaVersion := "2.12.6",

  scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:existentials"),

  organization := "xyz.hyperreal",

  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",

  resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven",


  mainClass in(Compile, run) := Some("xyz.hyperreal." + "btree" + ".TestMain"),

  publishMavenStyle := true,

  publishArtifact in Test := false,

  pomIncludeRepository := { _ => false },

  licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC")),

  homepage := Some(url("https://github.com/edadma/b-tree")),

  pomExtra :=
    <scm>
      <url>git@github.com:edadma/b-tree.git</url>
      <connection>scm:git:git@github.com:edadma/b-tree.git</connection>
    </scm>
      <developers>
        <developer>
          <id>edadma</id>
          <name>Edward A. Maxedon, Sr.</name>
          <url>https://github.com/edadma</url>
        </developer>
      </developers>
)

val btree = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.13.4" % Test
    )
  )