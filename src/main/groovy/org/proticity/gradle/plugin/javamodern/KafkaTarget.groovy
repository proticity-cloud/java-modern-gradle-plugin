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
