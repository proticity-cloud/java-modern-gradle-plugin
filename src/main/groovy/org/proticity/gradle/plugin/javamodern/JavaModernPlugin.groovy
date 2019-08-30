/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019 John Stewart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.proticity.gradle.plugin.javamodern

import com.fkorotkov.gradle.libraries.LibrariesPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.spotbugs.SpotBugsExtension
import com.github.spotbugs.SpotBugsPlugin
import com.github.spotbugs.SpotBugsTask
import com.jfrog.bintray.gradle.BintrayExtension
import com.palantir.jacoco.JacocoFullReportPlugin
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import net.researchgate.release.ReleaseExtension
import net.researchgate.release.ReleasePlugin
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import org.danilopianini.gradle.javadociolinker.JavadocIOLinker
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.javamodularity.moduleplugin.ModuleSystemPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.bintray.BintrayPlugin
import org.kordamp.gradle.plugin.buildscan.BuildScanPlugin
import org.kordamp.gradle.plugin.functionaltest.FunctionalTestPlugin
import org.kordamp.gradle.plugin.integrationtest.IntegrationTestPlugin
import org.kordamp.gradle.plugin.jacoco.JacocoPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin
import org.kordamp.gradle.plugin.licensing.LicensingPlugin
import org.kordamp.gradle.plugin.publishing.PublishingPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin
import org.kordamp.gradle.plugin.sourcexref.SourceXrefPlugin
import org.kordamp.gradle.plugin.testing.TestingPlugin
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.proticity.gradle.plugin.idea.IdeaPlusExtension
import org.proticity.gradle.plugin.idea.IdeaPlusPlugin
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin
import org.sonarqube.gradle.SonarQubeProperties
import org.sonarqube.gradle.SonarQubeTask
import se.patrikerdes.UseLatestVersionsPlugin

import java.nio.file.Paths

/**
 * The plugin which preconfigures Java projects.
 */
class JavaModernPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def ext = project.extensions.create('javaModern', JavaModernExtension, project)

        applyRepositories(project)
        applyPlugins(project, ext)
        applyDependencyManagement(project, ext.cloud)
    }

    protected void applyRepositories(Project project) {
        project.logger.debug('Adding default repositories.')
        project.repositories {
            mavenLocal()
            jcenter()
        }
    }

    /**
     * Apply the included plugins.
     *
     * @param project the project.
     */
    private static void applyPlugins(Project project, JavaModernExtension ext) {
        // Apply default plugins.
        project.plugins.apply(CheckstylePlugin)
        project.extensions.findByType(CheckstyleExtension).with {
            ignoreFailures = true
            toolVersion = '8.20'
        }

        // Configure Java builds for modularity.
        if (getJavaRelease(project) >= JavaVersion.VERSION_1_9) {
            project.plugins.apply(ModuleSystemPlugin)
        }

        // Enable the common Kordamp project configuration.
        project.plugins.apply(BasePlugin)
        def kordampExt = project.extensions.findByType(ProjectConfigurationExtension)

        // Configure Kordamp's licensing support.
        project.plugins.apply(LicensingPlugin)
        if (kordampExt.licensing.licenses.empty) {
            kordampExt.licensing {
                licenses {
                    license {
                        id = 'Apache-2.0'
                    }
                }
            }
        }
        def downloadLicensesExt = project.extensions.findByType(DownloadLicensesExtension)
        downloadLicensesExt.dependencyConfiguration = 'runtimeClasspath'
        downloadLicensesExt.includeProjectDependencies = true

        // Setup signatures.
        def signingKey = readProperty(project, 'signing.key', 'SIGNING_KEY', null)
        if (signingKey != null) {
            project.plugins.apply(SigningPlugin)
            project.extensions.findByType(SigningExtension).with {
                useInMemoryPgpKeys(
                        signingKey,
                        readProperty(project, 'signing.password', 'SIGNING_PASSWORD', ''))
            }
        }

        // Configure Kordamp's publishing support.
        project.plugins.apply(PublishingPlugin)
        project.plugins.apply(BintrayPlugin)
        // Predefine Sonatype OSSRH repositories for potential publishing.
        kordampExt.info {
            repositories {
                repository {
                    name = 'ossrh-releases'
                    url = 'https://oss.sonatype.org/service/local/staging/deploy/maven2'
                    credentials {
                        username = readProperty(project, 'ossrh.user', 'OSSRH_USER', '**UNDEFINED**')
                        password = readProperty(project, 'ossrh.password', 'OSSRH_PASSWORD', '**UNDEFINED**')
                    }
                }
                repository {
                    name = 'ossrh-snapshots'
                    url = 'https://oss.sonatype.org/content/repositories/snapshots'
                    credentials {
                        username = readProperty(project, 'ossrh.user', 'OSSRH_USER', '**UNDEFINED**')
                        password = readProperty(project, 'ossrh.password', 'OSSRH_PASSWORD', '**UNDEFINED**')
                    }
                }
            }
        }
        kordampExt.bintray {
            credentials {
                username = readProperty(project, 'bintray.user', 'BINTRAY_USER', '**UNDEFINED**')
                password = readProperty(project, 'bintray.key', 'BINTRAY_KEY', '**UNDEFINED**')
            }
        }
        project.afterEvaluate {
            project.extensions.findByType(BintrayExtension).with {
                pkg.githubRepo = null
            }
        }
        project.extensions.findByType(PublishingExtension).with { PublishingExtension pubExt ->
            if (!pubExt) {
                return
            }
            pubExt.repositories {
                def ciProjectId = System.getenv('CI_PROJECT_ID')
                if (ciProjectId) {
                    it.maven {
                        name = 'gitlab-ci'
                        url = "https://gitlab.com/api/v4/projects/${ciProjectId}/packages/maven"
                        credentials(HttpHeaderCredentials) {
                            name = 'Job-Token'
                            value = "${System.getenv('CI_JOB_TOKEN')}"
                        }
                        authentication {
                            header(HttpHeaderAuthentication)
                        }
                    }
                }
            }
        }

        // Apply the Kordamp JAR plugin
        project.plugins.apply(JarPlugin)

        // Apply Kordamp build scan plugin
        project.plugins.apply(BuildScanPlugin)

        // Configure JavaDoc generation
        project.plugins.apply(JavadocIOLinker)
        project.plugins.apply(JavadocPlugin)
        project.plugins.apply(SourceJarPlugin)
        project.plugins.apply(SourceXrefPlugin)
        kordampExt.javadoc {
            options {
                // When building for Java 8 make this explicit. This is necessary if the JDK is Java 9+ because it will
                // fail when linking to the Java 8 APIs due to its lack of module info. Forcing Java 8 as the target
                // allows JavaDoc to avoid looking for module info at all.
                if (getJavaRelease(project) < JavaVersion.VERSION_1_9) {
                    addStringOption('source', '8')
                }
                showFromProtected()
                linkSource = true
            }
        }

        // Configure testing setup
        project.plugins.apply(TestingPlugin)
        project.plugins.apply(IntegrationTestPlugin)
        project.plugins.apply(FunctionalTestPlugin)
        project.plugins.apply(JacocoPlugin)
        project.plugins.apply(JacocoFullReportPlugin)

        // IDEA plugin MUST not be applied when Kordamp's Integration and Functional test plugins are loaded. It must
        // come after. This is a side effect of the way Kordamp will configure the plugin if it is detected as applied
        // when those plugins apply. It will cause failure when importing into IntelliJ due to a bug in Gradle (not
        // Kordamp). Set it up manually here in a safe way.
        project.plugins.apply(IdeaPlusPlugin)
        project.extensions.findByType(IdeaPlusExtension).with {
            module {
                testSourceDirs += project.sourceSets.integrationTest.java.srcDirs
                testSourceDirs += project.sourceSets.functionalTest.java.srcDirs
                testResourceDirs += project.sourceSets.integrationTest.resources.srcDirs
                testResourceDirs += project.sourceSets.functionalTest.resources.srcDirs
                scopes.TEST.plus += [project.configurations.integrationTestCompile,
                                     project.configurations.functionalTestCompile]
            }
        }

        // Configure release
        def envGradleReleaseVersion = System.getenv('GRADLE_RELEASE_VERSION')
        def envGradleReleaseNewVersion = System.getenv('GRADLE_RELEASE_NEW_VERSION')
        if (!System.getProperty('release.useAutomaticVersion') && envGradleReleaseVersion
                && envGradleReleaseNewVersion) {
            System.setProperty('release.useAutomaticVersion', 'true')
        }
        if (!System.getProperty('release.releaseVersion') && envGradleReleaseVersion) {
            System.setProperty('release.releaseVersion', envGradleReleaseVersion)
        }
        if (!System.getProperty('release.newVersion') && envGradleReleaseNewVersion) {
            System.setProperty('release.newVersion', envGradleReleaseNewVersion)
        }
        project.plugins.apply(ReleasePlugin)
        project.extensions.findByType(ReleaseExtension).with {
            failOnUnversionedFiles = false
        }

        // Configure bug checks
        project.plugins.apply(SpotBugsPlugin)
        project.plugins.apply(DependencyCheckPlugin)
        project.plugins.apply(SonarQubePlugin)
        project.dependencies.add('spotbugsPlugins',
                'com.h3xstream.findsecbugs:findsecbugs-plugin:1.7.1')
        project.extensions.findByType(SpotBugsExtension).with {
            effort = 'max'
            reportLevel = 'low'
            toolVersion = '4.0.0-beta1'

            def spotBugsExclusionPath = Paths.get(project.projectDir.path, 'spotbugs-filter.xml')
            def spotBugsExclusionFile = new File(spotBugsExclusionPath.toString())
            if (spotBugsExclusionFile.exists()) {
                project.logger.info("Found default SpotBugs exclusion file.")
                excludeFilter = spotBugsExclusionFile
            }
        }
        project.tasks.withType(SpotBugsTask) {
            reports {
                xml.enabled = true
                html.enabled = false
            }
        }
        project.extensions.findByType(DependencyCheckExtension).with {
            format = ReportGenerator.Format.XML
        }
        project.extensions.findByType(SonarQubeExtension).with {
            def testDirs = filterAbsentFiles(project, project.sourceSets.test.allSource.srcDirs)
            def integrationTestDirs = filterAbsentFiles(project, project.sourceSets.integrationTest.allSource.srcDirs)
            def functionalTestDirs = filterAbsentFiles(project, project.sourceSets.functionalTest.allSource.srcDirs)
            testDirs.addAll(integrationTestDirs)
            testDirs.addAll(functionalTestDirs)
            def testSourceDirs = testDirs.join(',')

            def sourceBranch = System.getProperty('sonar.branch.name') ?:
                    project.findProperty('sonar.branch.name') ?:
                            System.getenv('GIT_BRANCH') ?:
                                System.getenv('CI_COMMIT_REF_NAME') ?:
                                        System.getenv('CI_MERGE_REQUEST_SOURCE_BRANCH_NAME') ?:
                                                System.getenv('TRAVIS_BRANCH') ?:
                                                        System.getenv('BITBUCKET_BRANCH') ?:
                                                                System.getenv("CIRCLE_BRANCH") ?:
                                                                        'master'
            def targetBranch = System.getProperty('sonar.branch.target') ?:
                    project.findProperty('sonar.branch.target') ?:
                            System.getenv('GIT_MERGE_BRANCH') ?:
                                    System.getenv('CI_MERGE_REQUEST_TARGET_BRANCH_NAME') ?:
                                            System.getenv('TRAVIS_PULL_REQUEST_BRANCH') ?:
                                                    System.getenv('BITBUCKET_PR_DESTINATION_BRANCH') ?:
                                                            System.getenv('GERRIT_BRANCH') ?:
                                                                    'master'
            project.logger.info("Detected source branch: ${sourceBranch}.")
            project.logger.info("Detected target branch: ${targetBranch}.")

            properties { SonarQubeProperties props ->
                props.property('sonar.branch.name', sourceBranch)
                props.property('sonar.tests', testSourceDirs)
                props.property('sonar.coverage.jacoco.xmlReportPaths',
                        "${project.buildDir}/reports/jacoco/jacocoFullReport/jacocoFullReport.xml")
                props.property('sonar.projectKey', System.getProperty('sonar.projectKey') ?:
                        "${project.group}:${project.name}")
                props.property('sonar.login', readProperty(project, 'sonar.login', 'SONAR_LOGIN',
                        '**UNDEFINED**'))
                props.property('sonar.host.url', readProperty(project, 'sonar.host.url', 'SONAR_URL',
                        'https://sonarcloud.io'))
                props.property('sonar.junit.reportPaths', "${project.buildDir}/test-results/test," +
                        "${project.buildDir}/test-results/integrationTest," +
                        "${project.buildDir}/test-results/functionalTest")
            }

            project.afterEvaluate {
                if (!properties.get('sonar.branch.target') && sourceBranch != ext.mainBranch) {
                    project.logger.info("Source branch ${sourceBranch} is not the main branch and no target set," +
                            " setting default of ${targetBranch}.")
                    properties { SonarQubeProperties props ->
                        props.property('sonar.branch.target', targetBranch)
                    }
                }
                if (!properties.get('sonar.organization')) {
                    try {
                        properties { SonarQubeProperties props ->
                            props.property('sonar.organization',
                                    readProperty(project, 'sonar.organization', 'SONAR_ORGANIZATION',
                                            kordampExt.info.organization.name.toLowerCase(Locale.ROOT)))
                        }
                    } catch (NullPointerException e) {
                        // Handle unset organization info; not likely since Kordamp encourages setting this.
                    }
                }
            }
        }
        project.tasks.withType(SonarQubeTask).each { SonarQubeTask t ->
            t.dependsOn('test', 'integrationTest', 'functionalTest', 'jacocoTestReport',
                    'jacocoIntegrationTestReport', 'jacocoFunctionalTestReport', 'jacocoFullReport')
        }

        // Configure dependency management
        project.plugins.apply(DependencyManagementPlugin)

        // Configure version management
        project.plugins.apply(VersionsPlugin)
        project.plugins.apply(UseLatestVersionsPlugin)
        project.plugins.apply(LibrariesPlugin)
    }

    /**
     * Apply dependency management versions.
     *
     * @param project the project.
     * @param cloudExt the extension with cloud target configuration.
     */
    private static void applyDependencyManagement(Project project, CloudExtension cloudExt) {
        project.logger.debug('Configuring dependency management')
        def depManagement = project.extensions.findByType(DependencyManagementExtension)

        def googleCloudVersion = '0.106.0-alpha'
        def springCloudVersion = '2.1.2.RELEASE'
        def jacksonVersion = '2.10.0.pr1'
        def hibernateVersion = '5.4.4.Final'
        def kafkaVersion = cloudExt.getTargetKafkaVersion()
        def zookeeperVersion = cloudExt.getTargetZookeeperVersion()
        def elasticSearchVersion = cloudExt.getTargetElasticsearchVersion()

        depManagement.dependencies {
            // Utilities
            dependency 'org.apache.commons:commons-lang3:3.9'
            dependency 'org.apache.commons:commons-collections4:4.4'
            dependency 'org.apache.commons:commons-compress:1.19'
            dependency 'org.apache.commons:commons-exec:1.3'
            dependency 'commons-io:commons-io:2.6'
            dependency 'commons-codec:commons-codec:1.13'
            dependency 'commons-math:commons-math:1.2'
            dependency 'com.google.guava:guava:28.0-jre'
            dependency 'com.google.inject:guice:4.2.2'

            // Testing dependencies
            dependencySet(group: 'org.junit.jupiter', version: '5.3.2') {
                entry 'junit-jupiter-api'
                entry 'junit-jupiter-engine'
            }
            dependency 'org.testng:testng:7.0.0'
            dependencySet(group: 'org.mockito', version: '3.0.0') {
                entry 'mockito-core'
                entry 'mockito-junit-jupiter'
            }
            dependency 'org.mockito:mockito-testng:0.1.1'
            dependency 'org.jmockit:jmockit:1.47'
            dependency 'org.easymock:easymock:4.0.2'
            dependency 'com.google.code.findbugs:jsr305:3.0.2'

            // Reactive streams tools
            dependency 'io.reactor:reactor-core:3.2.11.RELEASE'
            dependency 'io.projectreactor.netty:reactor-netty:0.8.10.RELEASE'
            dependency 'io.reactivex.rxjava2:rxjava:2.2.12'
            dependency 'io.reactivex.rxjava:rxkotlin:2.4.0'

            // Application insights
            dependency 'org.slf4j:slf4j-api:1.7.26'
            dependency 'org.apache.logging.log4j:log4j-slf4j-impl:2.12.1'
            dependency 'com.github.speedwing:log4j-cloudwatch-appender:0.1.0'
            dependency 'ch.qos.logback:logback-classic:1.3.0-alpha4'
            dependency 'io.github.dibog:cloudwatch-logback-appender:2.0.0'
            dependency 'com.microsoft.azure:applicationinsights-logging-logback:2.5.0-BETA.3'
            dependency "com.google.cloud:google-cloud-logging-logback:${googleCloudVersion}"
            dependencySet(group: 'io.micrometer', version: '1.2.0') {
                entry 'micrometer-core'
                entry 'micrometer-registry-influx'
                entry 'micrometer-registry-datadog'
                entry 'micrometer-registry-signalfx'
                entry 'micrometer-registry-jmx'
                entry 'micrometer-registry-graphite'
                entry 'micrometer-registry-cloudwatch'
                entry 'micrometer-registry-azure-monitor'
                entry 'micrometer-registry-stackdriver'
            }
            dependency 'io.opentracing:opentracing-api:0.33.0'
            dependency 'io.opentracing.contrib:opentracing-aws-sdk-2:0.1.2'
            dependency 'io.opentracing.contrib:opentracing-aws-sdk-1:0.1.2'
            dependency 'io.opentracing.contrib:opentracing-metrics:0.3.0'

            // Security
            dependencySet(group: 'org.bouncycastle', version: '1.62') {
                entry 'bcprov-jdk15on'
                entry 'bcprov-ext-jdk15on'
                entry 'bcpkix-jdk15on'
                entry 'bcmail-jdk15on'
                entry 'bcpg-jdk15on'
            }

            // Serialization
            dependencySet(group: 'com.fasterxml.jackson.core', version: jacksonVersion) {
                entry 'jackson-core'
                entry 'jackson-annotations'
                entry 'jackson-databind'
            }
            dependency "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${jacksonVersion}"
            dependency 'com.google.code.gson:gson:2.8.5'
            dependency 'javax.xml.bind:jaxb-api:2.3.1'
            dependency 'org.glassfish.jaxb:jaxb-runtime:2.3.1'
            dependency 'org.eclipse.persistence:org.eclipse.persistence.moxy:2.7.4'
            dependency 'xerces:xercesImpl:2.12.0'

            // Network frameworks
            dependency 'javax.servlet:javax.servlet-api:4.0.1'
            dependencySet(group: 'org.apache.tomcat.embed', version: '9.0.24') {
                entry 'tomcat-embed-core'
                entry 'tomcat-embed-jasper'
                entry 'tomcat-embed-el'
                entry 'tomcat-embed-websocket'
            }

            // Persistence
            dependency 'com.github.derjust:spring-data-dynamodb:5.1.0'
            dependency 'com.microsoft.spring.data.gremlin:spring-data-gremlin:2.1.7'
            dependency 'io.r2dbc:r2dbc-postgresql:1.0.0.M7'
            dependency 'io.r2dbc:r2dbc-mssql:1.0.0.M7'
            dependency 'io.r2dbc:r2dbc-h2:1.0.0.M7'
            dependency 'org.postgresql:postgresql:42.2.6'
            dependency 'mysql:mysql-connector-java:8.0.17'
            dependency 'org.mariadb.jdbc:mariadb-java-client:2.4.3'
            if (getJavaRelease(project) >= JavaVersion.VERSION_12) {
                dependency 'com.microsoft.sqlserver:mssql-jdbc:7.4.1.jre12'
            } else if (getJavaRelease(project) >= JavaVersion.VERSION_11) {
                dependency 'com.microsoft.sqlserver:mssql-jdbc:7.4.1.jre11'
            } else {
                dependency 'com.microsoft.sqlserver:mssql-jdbc:7.4.1.jre8'
            }
            dependency 'org.hsqldb:hsqldb:2.5.0'
            dependency 'com.h2database:h2:1.4.199'
            dependencySet(group: 'com.datastax.oss', version: '4.2.0') { // Cassandra
                entry 'java-driver-core'
                entry 'java-driver-query-driver'
            }
            dependencySet(group: 'org.mongodb', version: '3.11.0') {
                entry 'mongodb-driver-sync'
                entry 'mongodb-driver-async'
            }
            dependency "org.elasticsearch.client:transport:${elasticSearchVersion}"
            dependency 'redis.clients:jedis:3.1.0'
            dependency 'io.lettuce:lettuce-core:5.1.8.RELEASE'
            dependency 'org.neo4j.driver:neo4j-java-driver:4.0.0-beta01'
            dependency 'org.neo4j:neo4j-jdbc-driver:3.4.0'
            dependency "org.hibernate:hibernate-core:${hibernateVersion}"
            dependency 'org.hibernate.validator:hibernate-validator:6.0.17.Final'
            dependency 'org.eclipse.persistence:eclipselink:2.7.4'

            // Integration
            dependency 'javax.jms:jms-api:1.1-rev-1'
            dependency 'org.zeromq:jeromq:0.5.1'
            dependency 'com.rabbitmq:amqp-client:5.7.3'
            dependencySet(group: 'org.apache.kafka', version: kafkaVersion) {
                entry 'kafka-streams'
                entry 'kafka-clients'
                entry 'connect-file'
                entry 'connect-runtime'
                entry 'connect-transforms'
                entry 'connect-json'
                entry 'kafka-log4j-appender'
            }
            dependency "org.apache.zookeeper:zookeeper:${zookeeperVersion}"
            dependencySet(group: 'org.apache.curator', version: '4.2.0') {
                entry 'curator-framework'
                entry 'curator-recipes'
                entry 'curator-client'
                entry 'curator-x-discovery'
                entry 'curator-x-async'
            }

            // Force modern ASM version, needed for even building with this plugin due to SpotBugs
            dependencySet(group: 'org.ow2.asm', version: '7.1') {
                entry 'asm'
                entry 'asm-analysis'
                entry 'asm-commons'
                entry 'asm-tree'
                entry 'asm-util'
            }

            // Spring Cloud Streams
            dependency 'org.springframework.cloud:spring-cloud-stream-binder-kinesis:1.2.0.RELEASE'
            dependency 'org.springframework.cloud:spring-cloud-stream-binder-kstream:1.3.4.RELEASE'
            dependency 'org.springframework.cloud:spring-cloud-stream-binder-kstream11:1.3.0.RELEASE'
            dependency 'org.springframework.cloud:spring-cloud-stream-binder-kafka11:1.3.0.RELEASE'
            dependency 'org.springframework.cloud:spring-cloud-stream-binder-redis:1.0.0.RELEASE'
        }

        depManagement.imports {
            // Sprint Boot
            mavenBom 'org.springframework.boot:spring-boot-dependencies:2.1.7.RELEASE'

            // Network frameworks
            mavenBom 'io.netty:netty-bom:4.1.39.Final'
            mavenBom 'org.eclipse.jetty:jetty-bom:9.4.20.v20190813'

            // Persistence
            mavenBom 'org.springframework.data:spring-data-releasetrain:Lovelace-SR10'
            mavenBom "org.hibernate.ogm:hibernate-ogm-bom:5.4.1.Final"

            // Public cloud SDKs
            mavenBom 'com.amazonaws:aws-java-sdk-bom:1.11.620'
            mavenBom 'software.amazon.awssdk:bom:2.7.33'
            mavenBom 'com.microsoft.azure:azure-bom:1.0.0.M1'
            mavenBom "com.google.cloud:google-cloud-bom:${googleCloudVersion}"

            // Spring Cloud BOMs
            mavenBom "org.springframework.cloud:spring-cloud-stream-dependencies:Germantown.RELEASE"
            mavenBom "org.springframework.cloud:spring-cloud-sleuth-dependencies:${springCloudVersion}"
            mavenBom "org.springframework.cloud:spring-cloud-bus-dependencies:${springCloudVersion}"
            mavenBom "org.springframework.cloud:spring-cloud-aws-dependencies:${springCloudVersion}"
            mavenBom "org.springframework.cloud:spring-cloud-task-dependencies:${springCloudVersion}"
            mavenBom "org.springframework.cloud:spring-cloud-security-dependencies:${springCloudVersion}"
            mavenBom "org.springframework.cloud:spring-cloud-netflix-dependencies:${springCloudVersion}"
            mavenBom 'org.springframework.cloud:spring-cloud-function-dependencies:2.1.1.RELEASE'
            mavenBom 'org.springframework.cloud:spring-cloud-gcp-dependencies:1.1.2.RELEASE'

            // Integration
            mavenBom 'io.grpc:grpc-bom:1.23.0'
        }
    }

    /**
     * Determines the target Java release for the build.
     *
     * This will return whichever is greater of the source or target Java version. If neither is specified the current
     * JDK's version will be used.
     *
     * @param project the project.
     *
     * @return The Java version being targeted.
     */
    private static JavaVersion getJavaRelease(Project project) {
        def compat = project.findProperty('sourceCompatibility') ?:
                project.findProperty('targetCompatibility')
        return compat ? JavaVersion.toVersion(compat) : JavaVersion.current()
    }

    /**
     * Reads a property value, falling back on environment variables or a default value if necessary.
     *
     * @param project the project.
     * @param property the name of the property to try reading.
     * @param env the name of the environment variable to fall back on if no property is defined.
     * @param defaultValue the default value to use if no environment variable or property is defined.
     *
     * @return The value of the property.
     */
    private static String readProperty(Project project, String property, String env, String defaultValue) {
        def prop = System.getProperty(property)
        if (prop) {
            return prop.toString()
        }
        prop = project.findProperty(property)
        if (prop) {
            return prop.toString()
        }
        def environment = System.getenv(env)
        if (environment) {
            return environment
        }
        return defaultValue
    }

    /**
     * Apply a filter to a set of files which removes those which do not exist.
     *
     * @param fileSet the original file set.
     *
     * @return A new set without any files from the original which do not exist.
     */
    private static Set<String> filterAbsentFiles(final Project project, final Set<File> fileSet) {
        def results = new HashSet(fileSet.size())
        for (def file : fileSet) {
            if (file.exists()) {
                if (file.isAbsolute()) {
                    results.add(file.getPath().substring(project.projectDir.getAbsolutePath().length() + 1))
                } else {
                    results.add(file.getPath())
                }
            }
        }
        return results
    }
}