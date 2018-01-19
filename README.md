# Finatra Seed Project
[Finatra](https://twitter.github.io/finatra/) seed Project

## Development Environments
### Requirements
* [Scala 2.12.x](http://www.scala-lang.org/)
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - [Typesafe Config(1.3.0) only supports Java 8](https://github.com/typesafehub/config#binary-releases)

### Dependencies
* [Finatra](https://twitter.github.io/finatra/) - Fast, testable, Scala services built on [TwitterServer](http://twitter.github.io/twitter-server/) and [Finagle](https://twitter.github.io/finagle).
* [Guice](https://github.com/google/guice/wiki/Motivation) - a lightweight dependency injection framework for Java 6 and above by Google.
* [ScalaTest](http://www.scalatest.org/) - A testing tool for Scala and Java developers.
* [TypesafeConfig](https://github.com/typesafehub/config) - Configuration library for JVM languages.
* [Logback](http://logback.qos.ch/) - The Generic, Reliable, Fast & Flexible Logging Framework.

## Development Resource Links
* [Scala School](http://twitter.github.io/scala_school/index.html)
* [Twitter Future](https://github.com/twitter/util#futures)
* [Finatra User Guide](http://twitter.github.io/finatra/user-guide/)
* [Finatra Presentations](http://twitter.github.io/finatra/presentations/)
* [Finatra Examples Projects](https://github.com/twitter/finatra/tree/master/examples)
* [Guice Wiki](https://github.com/google/guice/wiki/Motivation)


## How to use This Template
Just git clone this project and use sbt
```bash
git clone https://github.com/genban/finatra-example-seed.git
```


## Getting Started

### Install sbt on Mac OSX using Homebrew
```bash
brew install sbt 
```
or
```bash
brew install typesafe-activator
```

### Start Server with sbt or activator

```bash
# clone repostitory
git clone https://github.com/genban/finatra-example-seed.git

cd finatra-example-seed/

# Start finatra server
sbt run 
...
[info] Loading project definition from finatra-example-seed/project
[info] Set current project to finatra-example-seed(in build file:finatra-example-seed/)
[info] Running FinatraServer
...
```


## Example Codes

### Add Sample Controller

```scala
// add new file SampleController.scala
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class SampleController extends Controller {

  // GET register /ping uri
  get("/ping") { request: Request =>
    // Define Response
    "pong"
  }
}
```

### Register Controller and Router on [FinatraServer]
```scala
import com.twitter.finatra.http.HttpServer
object FinatraServerMain extends FinatraServer

class FinatraServer extends HttpServer {

  override def configureHttp(router: HttpRouter) {
    router
      .add[SampleController]  // Register your Controller
  }
}
```

## Auto Restart FinatraServer using [spray/sbt-resolver](https://github.com/spray/sbt-revolver)
```bash
sbt "~re-start"
...
[info] Loading project definition from finatra-example-seed/project
[info] Set current project to finatra-example-seed(in build file:finatra-example-seed/)
...
```

## Create a fat JAR with compact all of its dependencies
```bash
# build single jar
sbt assembly
...
[info] Assembly up to date: finatra-example-seed/target/scala-2.11/finatra-example-seed.jar
[success] Total time: 10 s, completed Dec 3, 2018 20:48:01 PM
```

## Start Standalone Server
```bash
cd target/scala-2.11/
# Development mode with default logback.xml
java -jar finatra-example-seed.jar -mode dev
# Production mode with specified logback config file
java -jar -Dlogback.configurationFile=conf/real-logback.xml finatra-example-seed.jar -mode real
```

## Run modes
### dev(default)
Run with [`resources/conf/dev.conf`](./src/main/resources/conf/dev.conf) & [`resources/logback.xml`](./src/main/resources/logback.xml)

### Custom run mode needs two files
* typesafe config : src/main/resources/conf/xxx.conf
* logback.xml : src/main/resources/conf/xxx-logback.xml
* Run `java -jar -Dlogback.configurationFile=conf/xxx-logback.xml finatra-example-seed.jar -mode xxx`

### Show [Twitter Server flags](http://twitter.github.io/finatra/user-guide/getting-started/#flags)
```bash
java -jar finatra-example-seed.jar -help
...
flags:
  -help='false': Show this help
  -http.announce='java.lang.String': Address to announce HTTP server to
  -http.name='http': Http server name
  -http.port=':9999': External HTTP server port
  -https.announce='java.lang.String': Address to announce HTTPS server to
  -https.name='https': Https server name
  -https.port='': HTTPs Port
  -key.path='': path to SSL key
  -local.doc.root='': File serving directory for local development
  -log.append='true': If true, appends to existing logfile. Otherwise, file is truncated.
  -log.async='true': Log asynchronously
  -log.async.maxsize='4096': Max queue size for async logging
  -log.level='INFO': Log level
  -log.output='/dev/stderr': Output file
  -maxRequestSize='5242880.bytes': HTTP(s) Max Request Size
# set run mode with `-mode`
  -mode='dev': application run mode [dev:default, alpha, sandbox, beta, real]
  -mustache.templates.dir='templates': templates resource directory
  -shutdown.time='1.minutes': Maximum amount of time to wait for pending requests to complete on shutdown
  -tracingEnabled='true': Tracing enabled
```

## Remote Debugging
```bash
sbt -jvm-debug <port> run
```
Reference : http://stackoverflow.com/questions/19473941/how-to-debug-play-application-using-activator

## Troubleshooting
`sbt run` or `activator run` raise dependency errors, clear ivy's cache files and retry run.
```bash
rm -rf ~/.ivy2/cache/
```
