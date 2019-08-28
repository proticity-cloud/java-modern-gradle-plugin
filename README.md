# Java Modern Gradle Plugin
This plugin provides a preconfigured advanced and complete configuration for
Java projects developed via Gradle. It was implemented to simplify development
for the Proticity project but works just as well for anyone else (and has
expanded scope to support other projects).

## Usage
The plugin is available from the
[Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.proticity.gradle.java-modern).

Add to your project by applying via the dependency spec method:
```groovy
plugins {
    id 'java-library'
    id 'org.proticity.gradle.java-modern' version '0.1.8'
}
```
...or by adding to your buildscript and applying manually:
```groovy
buildscript {
    repositories {
        maven {
            url 'https://plugins.gradle.org/m2/'
        }
        jcenter()
    }
    dependencies {
        classpath 'org.proticity:java-modern-gradle-plugin:0.1.8'
    }
}

apply plugin: 'org.proticity.gradle.java-modern'
```

The following sampe project shows how simple it is to get a complete Java project:

```groovy
plugins {
    id 'java-library'
    id 'org.proticity.gradle.java-modern' version '0.1.9'
}

config {
    info {
        name = 'JDK 11 Sample Project'
        description = 'A sample project built on JDK 11.'
        vendor = 'Proticity'

        scm {
            url = 'https://gitlab.com/proticity/cloud/java-modern-gradle-plugin'
        }

        organization {
            url = 'https://www.proticity.org'
        }
    }
}
```
This particular `build.gradle` file is sufficient to build and assemble a Java project, create a source JAR, generate
JavaDocs with links to all dependencies and package it as a JavaDoc JAR, run unit, integration, and functional tests
with code coverage reporting, check for coding and security bugs, scan for known vulnerable dependencies, validate the
source code style, and (with some properties specified) publish Bintray. With only a little more it could publish to any
arbitrary Maven repository.

## Requirements
The current goal is to support the plugin for JDKs starting with the oldest LTS release of the set of newest LTS
releases supported on each major cloud service (AWS, Microsoft Azure, and Google Cloud), as well as all LTS releases
after that one, and the latest JDK (if the latest JDK is not an LTS release). The plugin is tested using builds of a
test project to support the following releases. 

| JVM        | Source            | JDK Version | Suported Build Releases    | Supported Target Releases |
| ---------- | ----------------- | ----------- | -------------------------- | ------------------------- |
| Corretto   | Amazon            | 8           | Any                        | Any                       |
| Corretto   | Amazon            | 11          | **11.0.3.7.1**<sup>†</sup> | Any                       |
| Liberica   | Bellsoft          | 8           | Any                        | Any                       |
| Liberica   | Bellsoft          | 11          | **11.31.11**<sup>†</sup>   | Any                       |
| Liberica   | Bellsoft          | 12          | **12.2.3**<sup>*</sup>     | Any                       |
| OpenJ9     | AdoptOpenJDK      | 8           | Any                        | Any                       |
| OpenJ9     | AdoptOpenJDK      | 11          | **11.0.3**<sup>†</sup>     | Any                       |
| OpenJ9     | AdoptOpenJDK      | 12          | **None**<sup>*</sup>       | Any                       |
| OpenJDK    | AdoptOpenJDK      | 8           | Any                        | Any                       |
| OpenJDK    | AdoptOpenJDK      | 11          | **11.0.3**<sup>†</sup>     | Any                       |
| OpenJDK    | AdoptOpenJDK      | 12          | **None**<sup>*</sup>       | Any                       |
| OpenJDK    | Oracle (Java.net) | 8           | Any                        | Any                       |
| OpenJDK    | Oracle (Java.net) | 11          | **11.0.3**<sup>†</sup>     | Any                       |
| OpenJDK    | Oracle (Java.net) | 12          | **12.0.1**<sup>*</sup>     | Any                       |
| OpenJDK    | Oracle (Java.net) | 13 EA       | **None**                   | **None**                  |
| SAPMachine | SAP               | 11          | **11.31.11**<sup>†</sup>   | Any                       |
| SAPMachine | SAP               | 12          | **12.0.1**<sup>*</sup>     | Any                       |
| Zulu       | Azul              | 8           | Any                        | Any                       |
| Zulu       | Azul              | 11          | **11.31.11**<sup>†</sup>   | Any                       |
| Zulu       | Azul              | 12          | **12.2.3**<sup>*</sup>     | Any                       |

<small><sup><sup>†</sup> `javadoc` may fail on Java 11.0.1 through 11.0.2 due to a bug which prevents JavaDoc from
running on projects with a `module-info.java` file. It will fail on all releases of Java 11 when a dependency is a
multi-release JAR if a JavaDoc is linked for the JAR which excludes the module definition.<br/>
<sup>*</sup> `javadoc` may fail on Java 12 GA releases due to a JavaDoc bug. To build on older Java 12 releases
disable JavaDocs. JavaDoc generation will fail on all JDK 12 releases if you have a multi-release JAR dependency and
add a JavaDoc link for the library which excludes the module definition.</small>

The recommended build toolchain is currently to build with OpenJDK 12.0.1 built from Oracle (the Java.net release), and
to target older JDK versions for 8 or 11. Java 11 and Java 12 GA have serious JavaDoc bugs and therefore not considered
fully supported, although they are tested and work with JavaDoc generation disabled.

## Features
### Default Repository Configuration
Out of the box application of the repositories for the Maven local cache (highest priority) and JCenter. To simplify
repository setup several additional convenience methods are available to add common repositories.

```groovy
repositories {
    gitlab() // Add global GitLab repository, for all public projects
    gitlab('proticity/cloud') // Add GitLab repository for a specific GitLab group
    gitlab(123456) // Add GitLab repository for a single project with the given ID
    springMilestones() // Add the Spring Milestones repository (releases only)
    springSnapshots() // Add the Spring Snapshots repository (snapshots only)
    ossrh() // Add Sonatype OSSRH repository, with snapshots and releases
    ossrhStaging() // Add Sonatype OSSRH staging repository
}
```

### Java Modules
Support is automatically included for Java modules development in JDK 11+ with the JavaModularity plugin. Additional
configuration is possible to build multi-release JARs that target JDK 1.8 but which also include `module-info` for JDK
11 and up to use:
```groovy
modularity {
    // Target Java 8, but add module-info which targets Java 9+, equivalent to mixedJavaRelease(8, 9).
    mixedJavaRelease(8)
    
    // Alternative arguments: target 8, but add module-info which targets Java 11+.
    mixedJavaRelease(8, 11)
}
```

### Kordamp Declarative Configuration
The [Kordamp plugin suite](https://aalmiray.github.io/kordamp-gradle-plugins/) is applied automatically. This allows
you to use the Kordamp plugins to declare project information Maven-style and have that information applied to other
plugin configuration automatically.

```groovy
config {
    // Project info config, added to POMs automatically
    info {
        name = '...'
        description = '...'
        ...
    }
    
    // Example declarative JavaDoc config
    javadoc {
        options {
            ...
        }
    }
    
    // Configuration for Bintray publishing example
    bintray {
        ...
    }
}
```

### JavaDoc Generation
JavaDocs are automatically generated for the `assemble` task. The JavaDocs default to including public and protected
members and include links to the source cross-reference. Your dependencies are scanned and JavaDoc links are
automatically generated for each Maven Central dependency using javadoc.io.

### Source Jar Generation
Automatically builds a source JAR during the `assemble` task, needed for publishing to repositories such as Sonatype's
OSSRH as well as being useful to consumers of your library.

### Maven Publishing
Out of the box support for publishing to Maven repositories and Bintray. A `main` publication is created automatically
via the Kordamp publishing plugin. The Kordamp bintray plugin is applied automatically as well to publish to Bintray if
configured. In addition to the default behavior of looking at project properties such a `bintray.user` and `bintray.key`
for credentials this plugin will allow the use of `BINTRAY_USER` and `BINTRAY_KEY` environment variables for better
integration into CI systems.

If you want to deploy directly to Maven Central rather than go through Bintray, the plugin comes with predefined
repositories for Sonatype's OSSRH, called `ossrh-snapshots` and `ossrh-releases`. You can configure these to be used
for deployment like so:

```groovy
config {
    publishing {
        releasesRepository = 'ossrh-releases'
        snapshotsRepository = 'ossrh-snapshots'
        signing = true
    }
}
```

An additional repository is defined for use by GitLab-hosted projects, named `gitlab-ci`. This repository will be
configured for a given project based on the environment variables automatically included in a GitLab CI pipeline.
Publishing to it will work automatically (without credentials being supplied) when run via GitLab CI.

It is necessary to enable signing for deploying directly to OSSRH. The credentials for deploying can be specified using
either the project/system properties `ossrh.user` and `ossrh.password` or the environment variables `OSSRH_USER` and
`OSSRH_PASSWORD`. The default signing behavior is to read an armor-encoded GPG private key from the property
`signing.key` or the environment variable `SIGNING_KEY`. The password for accessing the key is given by the property
`signing.password` or the environment variable `SIGNING_PASSWORD`.

### Code Quality and Security Checks
Spotbugs is included automatically, including the FindSecBugs extension. Reports are output in XML for further
processing (e.g. by the GitLab plugins for merge request integration). OWASP dependency scans are also available using
the `dependencyCheckAnalyze` task to find dependencies with known security vulnerabilities.

For more advanced scanning SonarQube support comes bundled. Unless otherwise specified it will default to using
SonarCloud as the SonarQube host, and your project key will default to `<groupId>:<projectName>` as per the SonarQube
recommendations. The host can be overridden with the standard properties for the SonarQube plugin or by setting the
`SONAR_URL` environment variable. Your SonarQube login token also has an environment variable available, the
`SONAR_LOGIN` variable. If not otherwise specified the organization will default to the project's organization name.
The branch reported to SonarQube will default to the current branch if it can be detected for your CI system (supports
GitLab, Travis CI, BitBucket, Gerrit, and Jenkins). The target branch will also be set if the pipeline being run is a
pull/merge request on a supported CI system (GitLab, GitHub w/ Travis CI, BitBucket, Gerrit w/ Jenkins). You can also
use the `GIT_BRANCH` and `GIT_MERGE_BRANCH` environment variables.

### Checkstyle Checks
Checkstyle scans are included by default. Scans are done automatically during the `check` target but it is configured
to not fail unless overridden.

### Testing
This plugin includes the Kordamp testing features, including integration and function testing. The `integrationTest` and
`functionalTest` goals are available and sources can be included in `src/integration-test/java` and
`src/functional-test/java`.

In addition to the Kordamp features, this plugin will automatically detect, for each task, if JUnit Jupiter or TestNG
is in the dependencies for its configurations and configure the framework to use automatically. Alternatively, you can
declare the framework to use for each test stage and it will be added to the dependencies automatically.

```groovy
javaModern {
    tests.useJUnitPlatform()
   
    integrationTests.andFunctionalTests.useTestNG {
        // TestNG configuration
    }
}
```

The `tests`, `integrationTests`, and `functionalTests` properties take the same `useJUnitPlatform()` and `useTestNG()`
methods that can be applied to test tasks. You can chain these properties together to apply the same config quickly to
multiple test types, e.g. `tests.andFunctionalTests` or `integrationTests.andTests`. When using these methods you do not
have to add JUnit or TestNG dependencies by hand. If Mockito is detected in your dependencies the proper Mockito
integration package will also be added.

It also automatically includes Jacoco coverage reporting for tests and supports not only Kordamp's merged reporting for
projects and subprojects but also merged reports within a single project between each type of test via Palantir's full
report plugin.


### IDEA Integration
This plugin applies the [IDEA-Plus Gradle Plugin](https://gitlab.com/proticity/cloud/idea-plus-gradle-plugin), which
provides enhanced features above and beyond the built-in IDEA plugin. When configuring IDEA integration you should use
the `ideaPlus` extension instead of the standard `idea` extension. Support for adding a bundled code style to an
IntelliJ project is available with the `applyIdeaCodeStyle` task and configured via the extension:

```groovy
ideaPlus {
    project {
        useProjectStyle = true
        codeStyle = resources.text.fromFile('src/main/codestyles/idea-codestyle.xml')
    }
}
```

### Release Management
The release plugin is automatically applied, allowing for automated release processes. The plugin will bump versions,
publish, tag the build, and rollback on failure. To better integrate with some CI environments it is possible to define
the version numbers of a release via environment variables rather than just properties. If the properties are not
declared then you can use the variables `GRADLE_RELEASE_VERSION` and `GRADLE_RELEASE_NEW_VERSION` instead. Automatic
versioning is enabled when both environment variables are present.

### Licence Management
Includes the Kordamp licensing plugin. This integrates license header checks into the `build` task and is preconfigured
to use the file `gradle/LICENSE_HEADER` in your project. It can also automatically add matching headers.

It has also preconfigured the `downloadLicenses` task for Gradle 5's differing dependency configuration, allowing it to
work out of the box on Gradle 5+ so that you can generate license reports or integrate with systems like GitLab's
license management reports on merge requests.

## Complete Plugin List
* Configuration
  * Kordamp Base
* Java Development
  * Gradle Java
  * Gradle IDEA
  * Java Modularity
* Documentation
  * Gradle Javadoc
  * Javadoc.io Linker
  * Kordamp Javadoc
  * Kordamp Source XRef
  * Kordamp Source JAR
* Testing
  * Kordamp Testing
  * Kordamp Integration Testing
  * Kordamp Functional Testing
  * Kordamp Jacoco
  * Palantir Jacoco Full Report
* Compliance
  * Gradle Licensing
  * Kordamp Licensing
  * Spotbugs
  * OWASP Dependency Check
  * Gradle Checkstyle
* Publishing
  * Gradle Maven Publishing
  * Gradle Signing
  * Kordamp Publishing
  * Kordamp Bintray
  * Gradle Release
* Version Management
  * Spring Dependency Management
  * Gradle Versioning
  * Gradle Use Latest Version
  * Gradle Libraries
* Debugging
  * Kordamp Build Scan

## Dependency Management
The following dependencies have default versions managed by the plugin (all which may be overridden if needed):

* Frameworks:
  * Spring Boot 2.x
* Networking:
  * Netty
  * Embedded Jetty
  * Embedded Tomcat
  * Servlet API 4.x
* Utility libraries:
  * Apache Commons IO, Collections 4, Lang 3, Codec, Compression, Math
  * Google Guice
  * Google Guava
  * Spring Framework
* Public cloud SDKs:
  * AWS SDKs 1.x and 2.x
  * Microsoft Azure
  * Google Cloud
  * Spring Cloud AWS, Spring Cloud Azure, and Spring Cloud GCP for Spring Boot integration
  * Spring Cloud Function for cloud-agnostic function-as-a-service
* Function reactive streams:
  * Reactor
  * Reactor Netty
  * RxJava/RxKotlin 2
* Persistence
  * JDBC drivers for PostgreSQL, MySQL, MariaDB, SQL Server, Oracle DB, H2, and HSQLDB, as well as Spring Data JDBC
  * R2DBC for reactive database connectivity, with drivers for PostgreSQL, SQL Server, and H2, as well as Spring Data
    R2DBC
  * Redis support via the non-blocking Jedis and Lettuce drivers, and Spring Data Redis
  * Cloud provider storage via DynamoDB, Neptune, Cosmos DB, and HBase
  * JPA providers Hibernate, Hibernate OGM, and EclipseLink, as well as Spring Data JPA
  * Hibernate Validators
  * ElasticSearch and Spring Data ElasticSearch
  * MongoDB and Spring Data MongoDB
  * Cassandra, with Spring Data Cassandra and Hibernate OGM support
  * Neo4j, including JDBC, Hibernate OGM, and Spring Data Neo4j support
  * Spring Data with support for the above backends
* Coordination and integration libraries
  * Apache Kafka (presets for latest and managed cloud provider versions)
  * Cloud messaging and data streams with AWS SQS/SNS, AWS Kinesis, Azure Event Hubs, and Google Pub/Sub
  * Apache Zookeeper
  * AMQP
  * ZeroMQ
  * JMX
  * Spring Messaging
  * Spring Cloud Streams and Spring Cloud Hub
* Serialization
  * Jackson
  * JSON
  * JAXB with backends from Glassfish (reference implementation), Moxy, and Xerces
* Testing
  * Junit 5 and TestNG for unit and integration testing
  * Mocking with Mockito, JMockit, and EasyMock
  * JSR305
* Debugging and Monitoring
  * SLF4J, with Logback and Log4j 2.x backends
  * Log appenders for AWS CloudWatch, Azure Application Insights, and Google Stackdriver
  * Micrometer metrics facade, with Influx, Graphite, CloudWatch, Application Insights, Stackdriver, Datadog, and
    SignalFX backends
  * OpenTracing tracing facade, including CloudWatch backend support
  * Spring Cloud Sleuth
  
### Targeting Specific Environments
When deploying to a public cloud you may be using a managed service with a specific version. Rather than requiring
all of the necessary dependencies to have a version specified and negating the benefits of the dependency management,
it is possible to describe the environment and the versions will be adapted automatically.

```groovy
javaModern {
    cloud {
        kafkaTarget = 'environment or version'
        zookeeperTarget = 'version'
        elasticSearchTarget = 'environment or version'
    }
}
```

| Property | Description | Values |
| -------- | ----------- | ------ |
| kafkaTarget | Sets which version of Kafka to use by default. | Set to a specific version number to use that version, or use 'aws_mks_1' for Kafka 1.x on AWS, 'aws_mks_2' for Kafka 2.x on AWS, or 'azure_event_hubs_1' for Kafka 1.x on Azure Event Hubs |
| zookeeperTarget | Override which version of Zookeeper to use by default. | Can be set to a specific version to use it for Zookeeper, otherwise syncs this to the Kafka version. |
| elasticSearchTarget| Sets which version of Elastic Search to use by default. | Set to a specific version number to use that version, or use 'aws_elk' to target the AWS Elastic Search Service. |