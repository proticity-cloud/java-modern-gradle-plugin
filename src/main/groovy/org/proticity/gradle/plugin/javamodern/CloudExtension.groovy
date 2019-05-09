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
 * Allows specification of cloud targets for the build.
 *
 * This can customize the dependency management or other features to target a given environment.
 */
class CloudExtension {
    /**
     * The target Kafka version configuration; this may be either the version you want or a KafkaTarget enum which
     * identifies the environment your project will be deployed to, which auto-selects the best known version.
     */
    Object kafkaTarget = KafkaTarget.ON_PREMISE

    /**
     * The target Zookeeper version configuration; this can be optionally set to fix a specific Zookeeper version or
     * left unspecified in which case it will be auto-configured for the Kafka version in use.
     */
    String zookeeperTarget

    /**
     * The target Elasticsearch version configuration; this may be either the version you want or an
     * ElasticsearchTarget enum value which specifies the environment it will be deployed to, in which case the best
     * known version is automatically picked.
     */
    Object elasticsearchTarget = ElasticsearchTarget.ON_PREMISE

    /**
     * The build project.
     */
    protected final Project project

    /**
     * Construct a new CloudExtension.
     *
     * @param project the project.
     */
    CloudExtension(Project project) {
        this.project = project
    }

    /**
     * Returns the target Kafka version for the given target configuration.
     *
     * @return The target Kafka version for the given target configuration.
     */
    String getTargetKafkaVersion() {
        if (kafkaTarget instanceof String) {
            return kafkaTarget
        } else if (kafkaTarget instanceof KafkaTarget) {
            switch (kafkaTarget) {
                case KafkaTarget.AWS_MKS_1:
                    return '1.1.1'
                case KafkaTarget.AWS_MKS_2:
                    return '2.1.1'
                case KafkaTarget.AZURE_EVENT_HUBS_1:
                    return '1.1.1'
                default:
                    return '2.2.0'
            }
        } else {
            throw new IllegalStateException('Kafka target must be a version string or target enum.')
        }
    }

    /**
     * Returns the target Zookeeper version for the given target configuration.
     *
     * @return The target Zookeeper version for the given target configuration.
     */
    String getTargetZookeeperVersion() {
        if (zookeeperTarget) {
            return zookeeperTarget
        }
        switch (getTargetKafkaVersion()) {
            case '0.10.1.1':
                return '3.4.8'
            case '0.10.2.0':
            case '0.10.2.1':
            case '0.10.2.2':
                return '3.4.9'
            case '0.11.0.0':
            case '0.11.0.1':
            case '0.11.0.2':
            case '0.11.0.3':
            case '1.0.0':
            case '1.0.1':
            case '1.0.2':
            case '1.1.0':
            case '1.1.1':
                return '3.4.10'
            case '2.0.0':
            case '2.0.1':
            case '2.1.0':
            case '2.1.1':
            case '2.2.0':
            default:
                return '3.4.13'
        }
    }

    /**
     * Returns the target Elasticsearch version for the configured target.
     *
     * @return The target Elasticsearch version for the configured target.
     */
    String getTargetElasticsearchVersion() {
        if (elasticsearchTarget instanceof String) {
            return elasticsearchTarget
        } else if (elasticsearchTarget instanceof ElasticsearchTarget) {
            switch (elasticsearchTarget) {
                case ElasticsearchTarget.ON_PREMISE:
                    return '7.0.0'
                case ElasticsearchTarget.AWS_ELK:
                    return '7.0.0'
            }
        } else {
            throw new IllegalStateException('Elasticsearch target must be a version string or target enum.')
        }
    }
}
