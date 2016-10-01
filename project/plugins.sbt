resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
      Resolver.ivyStylePatterns)

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.untyped" % "sbt-sass" % "0.8-M3")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

addSbtPlugin("me.lessis" % "coffeescripted-sbt" % "0.2.3")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

//addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.3")


// SASS (sbt-web)

//resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)

//addSbtPlugin("default" % "sbt-sass" % "0.1.9")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin( "me.lessis" % "bintray-sbt" % "0.3.0" )
