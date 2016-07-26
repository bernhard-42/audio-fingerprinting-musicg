name := "AudioFingerprinting"

version := "1.0"

scalaVersion := "2.10.6"

// mainClass in assembly := Some("com.betaocean.audiofingerprint.AudioFingerprint")

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "2.4.2",
  "com.h2database"  %  "h2"                % "1.4.192",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.7"
)


// include musicg project
unmanagedSourceDirectories in Compile += baseDirectory.value / "musicg/src"
unmanagedSourceDirectories in Compile += baseDirectory.value / "musicg/graphic"

excludeFilter in unmanagedSources := HiddenFileFilter || "*experiment*" || "*main*" || "*pitch*"
