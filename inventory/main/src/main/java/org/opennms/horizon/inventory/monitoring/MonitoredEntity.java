/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.inventory.monitoring;

import com.google.protobuf.Any;
import lombok.Builder;
import lombok.Getter;

/**
 * A monitored entity representing a single thing to monitor.
 * Actively monitored instances are transformed to {@link org.opennms.horizon.inventory.dto.MonitoredServiceDTO}s and
 * send to the minions as part of a monitoring {@link org.opennms.taskset.contract.TaskSet}.
 * <p>
 * These entities are created by implementations of {@link MonitoredEntityProvider}.
 */
@Builder
public class MonitoredEntity {
    /**
     * The provider which created this instance.
     */
    @Getter
    private final MonitoredEntityProvider source;

    /**
     * The ID of the monitored entity.
     * This ID is unique in the context of the {@link MonitoredEntityProvider} creating this instance.
     */
    private final String entityId;

    /**
     * The location this entity is monitored from.
     */
    @Getter
    private final long locationId;

    /**
     * The service type.
     * This types determines which service monitor plugin is used to monitor the service.
     */
    @Getter
    private final String type;

    /**
     * The configuration for the service.
     * This is highly dependent on {@link #type} and contains all info re
     */
    @Getter
    private final Any config;

    /**
     * Returns the global unique ID of the monitored entity.
     * @return the ID
     */
    public String getId() {
        return joinId(this.source.getProviderId(), this.entityId);
    }

    public static String joinId(final String providerId, final String entityId) {
        return providerId + ":" + entityId;
    }
}
