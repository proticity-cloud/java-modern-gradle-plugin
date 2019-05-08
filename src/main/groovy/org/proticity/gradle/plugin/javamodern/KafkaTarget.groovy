package org.proticity.gradle.plugin.javamodern

/**
 * Specifies the target Kafka environment for Kafka dependency management.
 */
enum KafkaTarget {
    /**
     * Use the default on-premise Kafka version management.
     */
    ON_PREMISE,

    /**
     * The Kafka target environment is Kafka 1.x on AWS MKS.
     */
    AWS_MKS_1,

    /**
     * The Kafka target environment is Kafka 2.x on AWS MKS.
     */
    AWS_MKS_2,

    /**
     * The Kafka target environment is Kafka 1.x on Azure Event Hubs.
     */
    AZURE_EVENT_HUBS_1
}
