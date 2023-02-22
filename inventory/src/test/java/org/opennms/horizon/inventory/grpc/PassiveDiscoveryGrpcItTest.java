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

package org.opennms.horizon.inventory.grpc;

import com.google.protobuf.ProtocolStringList;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.VerificationException;
import org.opennms.horizon.inventory.SpringContextTestInitializer;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryServiceGrpc;
import org.opennms.horizon.inventory.dto.TagCreateDTO;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(initializers = {SpringContextTestInitializer.class})
class PassiveDiscoveryGrpcItTest extends GrpcTestBase {
    private static final String DEFAULT_LOCATION = "Default";
    private static final String TEST_TAG_NAME_1 = "tag-name-1";
    private static final int TEST_SNMP_PORT = 161;
    private static final String TEST_SNMP_COMMUNITY = "public";

    private PassiveDiscoveryServiceGrpc.PassiveDiscoveryServiceBlockingStub serviceStub;

    @BeforeEach
    public void prepare() throws VerificationException {
        prepareTestGrpc();
        prepareServer();
        serviceStub = PassiveDiscoveryServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void cleanUp() throws InterruptedException {
        afterTest();
    }

    @Test
    void testCreatePassiveDiscovery() {
        TagCreateDTO tagCreateDto1 = TagCreateDTO.newBuilder().setName(TEST_TAG_NAME_1).build();

        PassiveDiscoveryCreateDTO createDTO = PassiveDiscoveryCreateDTO.newBuilder()
            .addLocations(DEFAULT_LOCATION)
            .addPorts(TEST_SNMP_PORT)
            .addCommunities(TEST_SNMP_COMMUNITY)
            .addAllTags(List.of(tagCreateDto1))
            .build();

        PassiveDiscoveryDTO passiveDiscovery = serviceStub.withInterceptors(MetadataUtils
                .newAttachHeadersInterceptor(createAuthHeader(authHeader)))
            .createDiscovery(createDTO);

        assertEquals(createDTO.getLocationsCount(), passiveDiscovery.getLocationsCount());
        assertTrue(passiveDiscovery.getToggle());
        assertTrue(passiveDiscovery.getCreateTimeMsec() > 0);

        assertEquals(createDTO.getPortsCount(), passiveDiscovery.getPortsCount());
        List<Integer> portsList = passiveDiscovery.getPortsList();
        assertEquals(TEST_SNMP_PORT, portsList.get(0));

        assertEquals(createDTO.getCommunitiesCount(), passiveDiscovery.getCommunitiesCount());
        ProtocolStringList communitiesList = passiveDiscovery.getCommunitiesList();
        assertEquals(TEST_SNMP_COMMUNITY, communitiesList.get(0));

        assertTrue(passiveDiscovery.getCreateTimeMsec() > 0);
    }
}
