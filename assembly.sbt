
// assembly for packaging as single jar
assemblyMergeStrategy in assembly := {
  case "BUILD"                                              => MergeStrategy.discard
  case "META-INF/io.netty.versions.properties"              => MergeStrategy.last
  case PathList("org", "apache", xs@_*)                     => MergeStrategy.last
  case PathList("com", "twitter", xs@_*)                    => MergeStrategy.last
  case PathList("com", "google", "code", "findbugs", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*)                     => MergeStrategy.last
  case PathList(xs@_*) if xs.last endsWith ".properties"    => MergeStrategy.last
  case other                                                =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(other)
}

assemblyJarName in assembly := s"${name.value}.jar"

assemblyOutputPath in assembly := new File(s"target/${name.value}.jar")