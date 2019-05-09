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

import org.gradle.api.Project

/**
 * An extension class for customizing the behavior of the JavaModernPlugin.
 */
class JavaModernExtension {
    /**
     * A nested extension object for customizing dependencies based on public cloud platforms being targeted.
     */
    CloudExtension cloud

    /**
     * The build project.
     */
    protected final Project project

    /**
     * Construct a new extension object.
     *
     * @param project the build project.
     */
    JavaModernExtension(Project project) {
        this.project = project
        cloud = new CloudExtension(project)
    }

    /**
     * Get a test configuration builder initialized for targeting unit tests.
     *
     * @return A test configuration builder initialized for targeting unit tests.
     */
    TestOptions getTests() {
        def configs = new HashSet<String>()
        configs.add('test')
        return new TestOptions(project, configs)
    }

    /**
     * Get a test configuration builder initialized for targeting integration tests.
     *
     * @return A test configuration builder initialized for targeting integration tests.
     */
    TestOptions getIntegrationTests() {
        def configs = new HashSet<String>()
        configs.add('integrationTest')
        return new TestOptions(project, configs)
    }

    /**
     * Get a test configuration builder initialized for targeting functional tests.
     *
     * @return A test configuration builder initialized for targeting functional tests.
     */
    TestOptions getFunctionalTests() {
        def configs = new HashSet<String>()
        configs.add('functionalTest')
        return new TestOptions(project, configs)
    }
}
