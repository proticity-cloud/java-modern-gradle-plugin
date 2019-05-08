package org.proticity.gradle.plugin.javamodern

/**
 * Specifies the target Elastic Search environment to use for dependency management.
 */
enum ElasticSearchTarget {
    /**
     * Use the default on-premise version management.
     */
    ON_PREMISE,

    /**
     * Target ElasticSearch versions for the Amazon ElasticSearch Service.
     */
    AWS_ELK
}
