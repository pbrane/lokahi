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
package org.opennms.horizon.server.mapper.alert;

import org.mapstruct.BeforeMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.opennms.horizon.alerts.proto.PolicyRuleProto;
import org.opennms.horizon.server.model.alerts.PolicyRule;

@Mapper(
        componentModel = "spring",
        uses = {AlertConditionMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PolicyRuleMapper {
    @Mapping(target = "alertConditions", source = "snmpEventsList")
    PolicyRule map(PolicyRuleProto proto);

    @Mapping(target = "snmpEventsList", source = "alertConditions")
    @Mapping(target = "labels", ignore = true)
    PolicyRuleProto map(PolicyRule rule);

    @BeforeMapping
    default void ensureLabelsInitialized(@MappingTarget PolicyRuleProto.Builder targetBuilder, PolicyRule source) {
        if (source.getLabels() != null) {
            targetBuilder.putAllLabels(source.getLabels()); // Initialize with an empty map if necessary
        }
    }
}
