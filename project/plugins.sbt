resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
      Resolver.ivyStylePatterns)

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

addSbtPlugin( "me.lessis" % "bintray-sbt" % "0.3.0" )