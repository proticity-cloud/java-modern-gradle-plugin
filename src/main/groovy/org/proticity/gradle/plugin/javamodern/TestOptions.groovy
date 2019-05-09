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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.testng.TestNGOptions

/**
 * Builds test setup options.
 */
class TestOptions {
    /**
     * The set of configurations to which this object applies.
     */
    private final Set<String> configurations

    /**
     * The build project.
     */
    private final Project project

    /**
     * Construct a new TestOptions.
     *
     * @param project the build target.
     * @param configurations the set of configurations to which this TestOptions will apply.
     */
    TestOptions(final Project project, final Set<String> configurations) {
        this.configurations = configurations
        this.project = project
    }

    /**
     * Adds on application of the builder to unit tests,
     *
     * @return The TestOptions.
     */
    TestOptions getAndTests() {
        configurations.add('test')
        return this
    }

    /**
     * Adds on application of the builder to integration tests.
     *
     * @return The TestOptions.
     */
    TestOptions getAndIntegrationTests() {
        configurations.add('integrationTest')
        return this
    }

    /**
     * Adds on application of the builder to functional tests.
     *
     * @return The TesTOptions.
     */
    TestOptions getAndFunctionalTests() {
        configurations.add('functionalTest')
        return this
    }

    /**
     * Determine if a given project configuration is using Mockito.
     *
     * @param configName The base name of the configuration.
     *
     * @return Whether Mockito is being used.
     */
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

    /**
     * Add JUnit dependencies to a given configuration.
     *
     * @param config the base name of the configurations to which JUnit will be added.
     */
    private void addJUnit(final String config) {
        project.dependencies.add(config + 'Implementation', 'org.junit.jupiter:junit-jupiter-api')
        project.dependencies.add(config + 'RuntimeOnly', 'org.junit.jupiter:junit-jupiter-engine')
        if (hasMockito(config)) {
            project.dependencies.add(config + 'Implementation', 'org.mockito:mockito-junit-jupiter')
        }
    }

    /**
     * Declares that the given test configurations should use JUnit Jupiter.
     */
    void useJUnitPlatform() {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useJUnitPlatform()
        }
    }

    /**
     * Declares that the given test configurations should use JUnit Jupiter.
     *
     * @param testFrameworkConfigure the test framework configuration closure.
     */
    void useJUnitPlatform(Action<? super TestNGOptions> testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useJUnitPlatform(testFrameworkConfigure)
        }
    }

    /**
     * Declares that the given test configurations should use JUnit Jupiter.
     *
     * @param testFrameworkConfigure the test framework configuration closure.
     */
    void useJUnitPlatform(Closure testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useJUnitPlatform(testFrameworkConfigure)
        }
    }

    /**
     * Add the TestNG dependencies to a given set of configurations.
     *
     * @param config the base name of the configurations to which dependencies are to be added.
     */
    private void addTestNG(final String config) {
        project.dependencies.add(config + 'Implementation', 'org.testng:testng')
        if (hasMockito(config)) {
            project.dependencies.add(config + 'Implementation', 'org.mockito:mockito-testng')
        }
    }

    /**
     * Declares that the given test configurations should use TestNG.
     */
    void useTestNG() {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useTestNG()
        }
    }

    /**
     * Declares that the given test configurations should use TestNG.
     *
     * @param testFrameworkConfigure the test framework configuration closure.
     */
    void useTestNG(Action<? super TestNGOptions> testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useTestNG(testFrameworkConfigure)
        }
    }

    /**
     * Declares that the given test configurations should use TestNG.
     *
     * @param testFrameworkConfigure the test framework configuration closure.
     */
    void useTestNG(Closure testFrameworkConfigure) {
        for (def config : configurations) {
            ((Test) project.tasks.findByName(config)).useTestNG(testFrameworkConfigure)
        }
    }
}
