package org.proticity.gradle.plugin.javamodern

import org.gradle.api.Project

class JavaModernExtension {
    CloudExtension cloud

    protected final Project project

    JavaModernExtension(Project project) {
        this.project = project
        cloud = new CloudExtension(project)
    }

    TestOptions getTests() {
        def configs = new HashSet<String>()
        configs.add('test')
        return new TestOptions(project, configs)
    }

    TestOptions getIntegrationTests() {
        def configs = new HashSet<String>()
        configs.add('integrationTest')
        return new TestOptions(project, configs)
    }

    TestOptions getFunctionalTests() {
        def configs = new HashSet<String>()
        configs.add('functionalTest')
        return new TestOptions(project, configs)
    }
}
