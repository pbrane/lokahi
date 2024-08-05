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
package org.opennms.horizon.inventory.monitoring.simple;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityRequest;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponse;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponseList;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface SimpleMonitoredEntityMapper {
    SimpleMonitoredEntityResponse map(SimpleMonitoredActiveDiscovery entity);

    SimpleMonitoredActiveDiscovery map(String tenantId, SimpleMonitoredEntityRequest request);

    default SimpleMonitoredEntityResponseList map(List<SimpleMonitoredActiveDiscovery> entities) {
        return SimpleMonitoredEntityResponseList.newBuilder()
                .addAllEntry(Lists.transform(entities, this::map))
                .build();
    }

    default Long map(String id) {
        if (Strings.isNullOrEmpty(id)) {
            return 0L;
        } else {
            return Long.parseLong(id);
        }
    }
}
