package org.proticity.gradle.plugin.javamodern

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.testng.TestNGOptions

class TestOptions {
    private final Set<String> configurations
    private final Project project;

    TestOptions(final Project project, final Set<String> configurations) {
        this.configurations = configurations
        this.project = project
    }

    TestOptions getAndTests() {
        configurations.add('test')
        return this
    }

    TestOptions getAndIntegrationTests() {
        configurations.add('integrationTest')
        return this
    }

    TestOptions getAndFunctionalTests() {
        configurations.add('functionalTest')
        return this
    }

    private boolean hasMockito(final String configName) {
        String[] configSuffixes = [ 'RuntimeClasspath', 'RuntimeOnly', 'CompileClasspath', 'CompileOnly' ]
        for (def configSuffix : configSuffixes) {
            def config = project.configurations.findByName(configName + configSuffix)
            if (!config) {
                break
            }
            for (dep in config.dependencies) {
                switch (dep.group) {
                    case 'org.mockito':
                        return true
                }
            }
        }
        return false
    }

    private void addJUnit(final String config) {
        project.dependencies.add(config + 'Implementation', 'org.junit.jupiter:junit-jupiter-api')
        project.dependencies.add(config + 'RuntimeOnly', 'org.junit.jupiter:junit-jupiter-engine')
        if (hasMockito(config)) {
            project.dependencies.add(config + 'Implementation', 'org.mockito:mockito-junit-jupiter')
        }
    }

    void useJUnitPlatform() {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useJUnitPlatform()
        }
    }

    void useJUnitPlatform(Action<? super TestNGOptions> testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useJUnitPlatform(testFrameworkConfigure)
        }
    }

    void useJUnitPlatform(Closure testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useJUnitPlatform(testFrameworkConfigure)
        }
    }

    private void addTestNG(final String config) {
        project.dependencies.add(config + 'Implementation', 'org.testng:testng')
        if (hasMockito(config)) {
            project.dependencies.add(configName + 'Implementation', 'org.mockito:mockito-testng')
        }
    }

    void useTestNG() {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useTestNG()
        }
    }

    void useTestNG(Action<? super TestNGOptions> testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useTestNG(testFrameworkConfigure)
        }
    }

    void useTestNG(Closure testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useTestNG(testFrameworkConfigure)
        }
    }
}
