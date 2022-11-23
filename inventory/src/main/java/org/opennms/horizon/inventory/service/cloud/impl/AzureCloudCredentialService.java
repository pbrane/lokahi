/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.horizon.inventory.service.cloud.impl;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.AzureCredentialsCreateDTO;
import org.opennms.horizon.inventory.dto.AzureCredentialsDTO;
import org.opennms.horizon.inventory.dto.CloudCredentialsDTO;
import org.opennms.horizon.inventory.mapper.cloud.CloudCredentialMapper;
import org.opennms.horizon.inventory.model.cloud.AzureCloudCredential;
import org.opennms.horizon.inventory.repository.cloud.AzureCloudCredentialRepository;
import org.opennms.horizon.inventory.service.cloud.CloudCredentialService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@RequiredArgsConstructor
@Service("azureCloudCredentialService")
public class AzureCloudCredentialService implements CloudCredentialService {
    private final CloudCredentialMapper mapper;
    private final AzureCloudCredentialRepository repository;

    @Override
    public CloudCredentialsDTO create(String tenantId, Any config) {

        if (!config.is(AzureCredentialsCreateDTO.class)) {
            throw new IllegalArgumentException("config must be an AzureCredentialsCreateDTO; type-url=" + config.getTypeUrl());
        }

        try {
            AzureCredentialsCreateDTO request = config.unpack(AzureCredentialsCreateDTO.class);

            //todo: encrypt client secret
            AzureCloudCredential credential = mapper.dtoToModel(request);
            credential.setTenantId(tenantId);
            credential.setCreateTime(LocalDateTime.now());
            credential = repository.save(credential);

            AzureCredentialsDTO savedCredentials = mapper.modelToDto(credential);

            return CloudCredentialsDTO.newBuilder()
                .setAzureCredentials(savedCredentials)
                .build();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to unpack azure credentials", e);
        }
    }
}
