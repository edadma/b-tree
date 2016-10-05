name := "b-tree"

version := "0.2"

scalaVersion := "2.11.8"

scalacOptions ++= Seq( "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:existentials" )

incOptions := incOptions.value.withNameHashing( true )

organization := "xyz.hyperreal"

//resolvers += Resolver.sonatypeRepo( "snapshots" )

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "3.0.0" % "test",
	"org.scalacheck" %% "scalacheck" % "1.13.2" % "test"
)

//libraryDependencies += "xyz.hyperreal" %% "ramfile" % "0.3"

mainClass in (Compile, run) := Some( "xyz.hyperreal." + "btree" + ".TestMain" )

mainClass in assembly := Some( "xyz.hyperreal." + "btree" + ".Main" )

assemblyJarName in assembly := "btree" + "-" + version.value + ".jar"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/edadma/b-tree"))

pomExtra := (
  <scm>
    <url>git@github.com:edadma/b-tree.git</url>
    <connection>scm:git:git@github.com:edadma/b-tree.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edadma</id>
      <name>Edward A. Maxedon, Sr.</name>
      <url>http://hyperreal.ca</url>
    </developer>
  </developers>)
