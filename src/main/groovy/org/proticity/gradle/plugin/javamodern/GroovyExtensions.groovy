package org.proticity.gradle.plugin.javamodern

import org.gradle.api.artifacts.dsl.RepositoryHandler

/**
 * Extensions for Groovy/Gradle types.
 */
class GroovyExtensions {
    /**
     * Add the GitLab global repository, which provides a single view of all projects' repositories.
     *
     * Note that GitLab does not control coordinates between projects, and two projects may publish artifacts with the
     * same coordinates (the most recent one overwrites the previous). It is much safer to use the group-specific
     * repositories.
     *
     * @param repositories the repository handler.
     */
    static void gitlab(final RepositoryHandler repositories) {
        repositories.maven {
            name 'gitlab'
            url 'https://gitlab.com/api/v4/packages/maven'
        }
    }

    /**
     * Add a GitLab group-specific repository.
     *
     * @param repositories the repository handler.
     * @param group the name of the GitLab group.
     */
    static void gitlab(final RepositoryHandler repositories, final String group) {
        repositories.maven {
            name "gitlab-${group}"
            url "https://gitlab.com/api/v4/groups/${group}/-/packages/maven"
        }
    }

    /**
     * Add a GitLab project-specific repository.
     *
     * @param repositories the repository handler.
     * @param projectId the ID of the project.
     */
    static void gitlab(final RepositoryHandler repositories, final int projectId) {
        repositories.maven {
            name "gitlab-${group}"
            url "https://gitlab.com/api/v4/projects/${projectId}/packages/maven"
        }
    }

    /**
     * Add the Sonatype OSSRH repository for snapshots and releases (note that if you only want releases, they are
     * synchronized to Maven Central).
     *
     * @param repositories the repository handler.
     */
    static void ossrh(final RepositoryHandler repositories) {
        repositories.maven {
            name 'ossrh'
            url 'https://oss.sonatype.org/content/groups/public'
        }
    }

    /**
     * Add the Sonatype OSSRH staging repository, for artifacts currently being staged before release.
     *
     * @param repositories the repository handler.
     */
    static void ossrhStaging(final RepositoryHandler repositories) {
        repositories.maven {
            name 'ossrh-staging'
            url 'https://oss.sonatype.org/content/groups/staging'
        }
    }

    /**
     * Add the Spring Milestones repository.
     *
     * @param repositories the repository handler.
     */
    static void springMilestones(final RepositoryHandler repositories) {
        repositories.maven {
            name 'spring-milestones'
            url 'https://repo.spring.io/milestone'
            mavenContent {
                releasesOnly()
            }
        }
    }

    /**
     * Add the Spring Snapshots repository.
     *
     * @param repositories the repository handler.
     */
    static void springSnapshots(final RepositoryHandler repositories) {
        repositories.maven {
            name 'spring-snapshots'
            url 'https://repo.spring.io/snapshot'
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}
