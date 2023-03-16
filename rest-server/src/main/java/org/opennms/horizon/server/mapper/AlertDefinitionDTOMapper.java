/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.horizon.server.mapper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.UInt64Value;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.opennms.horizon.alerts.proto.AlertDefinition;
import org.opennms.horizon.alerts.proto.ListAlertDefinitionsRequest;
import org.opennms.horizon.alerts.proto.ListAlertDefinitionsResponse;
import org.opennms.horizon.server.model.alert.AlertDefinitionDTO;
import org.opennms.horizon.server.model.alert.ListAlertDefinitionsRequestDTO;
import org.opennms.horizon.server.model.alert.ListAlertDefinitionsResponseDTO;

@Mapper(componentModel = "spring", uses = {EventMatchDTOMapper.class},
    // Needed for grpc proto mapping
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface AlertDefinitionDTOMapper {

    @Mapping(target = "definitions", source = "definitionsList", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    ListAlertDefinitionsResponseDTO protoToListAlertDefinitionsResponseDTO(ListAlertDefinitionsResponse list);

    @Mapping(target = "definitionsList", source = "definitions", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    ListAlertDefinitionsResponse ListAlertDefinitionsRequestDTOToProto(ListAlertDefinitionsResponseDTO list);

    @Mapping(target = "match", source = "matchList", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    AlertDefinitionDTO protoToAlertDefinitionDTO(AlertDefinition alert);

    @Mapping(target = "matchList", source = "match", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    AlertDefinition alertDefinitionDTOToProto(AlertDefinitionDTO alert);

    ListAlertDefinitionsRequest listAlertDefinitionsRequestDTOtoProto(ListAlertDefinitionsRequestDTO request);

    default UInt64Value longToUInt64Value(Long value) {
        return UInt64Value.of(value);
    }

}
