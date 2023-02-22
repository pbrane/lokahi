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

package org.opennms.horizon.server.service;

import io.leangen.graphql.execution.ResolutionEnvironment;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryResponseDTO;
import org.opennms.horizon.server.RestServerApplication;
import org.opennms.horizon.server.service.grpc.InventoryClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = RestServerApplication.class)
class GraphQLPassiveDiscoveryServiceTest {
    private static final String GRAPHQL_PATH = "/graphql";
    @MockBean
    private InventoryClient mockClient;
    @Autowired
    private WebTestClient webClient;
    @MockBean
    private ServerHeaderUtil mockHeaderUtil;
    private final String accessToken = "test-token-12345";
    private PassiveDiscoveryDTO passiveDiscoveryDTO;

    @BeforeEach
    public void setUp() {
        passiveDiscoveryDTO = PassiveDiscoveryDTO.newBuilder()
            .setId(1L)
            .addLocations("Default")
            .setToggle(true)
            .addAllPorts(List.of(161))
            .addAllCommunities(List.of("public"))
            .setCreateTimeMsec(Instant.now().toEpochMilli())
            .build();

        doReturn(accessToken).when(mockHeaderUtil).getAuthHeader(any(ResolutionEnvironment.class));
    }

    @AfterEach
    public void afterTest() {
        verifyNoMoreInteractions(mockClient);
        verifyNoMoreInteractions(mockHeaderUtil);
    }

    @Test
    void testCreatePassiveDiscovery() throws JSONException {
        doReturn(passiveDiscoveryDTO).when(mockClient).createPassiveDiscovery(any(PassiveDiscoveryCreateDTO.class), eq(accessToken));

        String request = createPayload("mutation { " +
            "    createPassiveDiscovery( " +
            "        discovery: { " +
            "            locations: [\"Default\"], " +
            "            snmpPorts: [ 161 ], " +
            "            snmpCommunities: [ \"public\" ], " +
            "            tags: [ " +
            "                { " +
            "                    name:\"tag-1\" " +
            "                } " +
            "            ] " +
            "        } " +
            "    ) { " +
            "        id, " +
            "        locations, " +
            "        toggle, " +
            "        snmpPorts, " +
            "        createTimeMsec " +
            "    } " +
            "}");

        webClient.post()
            .uri(GRAPHQL_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.createPassiveDiscovery.id").isEqualTo(passiveDiscoveryDTO.getId())
            .jsonPath("$.data.createPassiveDiscovery.toggle").isEqualTo(passiveDiscoveryDTO.getToggle())
            .jsonPath("$.data.createPassiveDiscovery.createTimeMsec").isEqualTo(passiveDiscoveryDTO.getCreateTimeMsec());
        verify(mockClient).createPassiveDiscovery(any(PassiveDiscoveryCreateDTO.class), eq(accessToken));
        verify(mockHeaderUtil, times(1)).getAuthHeader(any(ResolutionEnvironment.class));
    }

    @Test
    void testGetPassiveDiscovery() throws JSONException {
        doReturn(PassiveDiscoveryResponseDTO.newBuilder().setDiscovery(passiveDiscoveryDTO).build())
            .when(mockClient).getPassiveDiscovery(eq(accessToken));

        String request = createPayload("query { " +
            "    passiveDiscovery { " +
            "        id, " +
            "        locations, " +
            "        toggle, " +
            "        snmpPorts, " +
            "        createTimeMsec " +
            "    } " +
            "}");

        webClient.post()
            .uri(GRAPHQL_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.passiveDiscovery.id").isEqualTo(passiveDiscoveryDTO.getId())
            .jsonPath("$.data.passiveDiscovery.toggle").isEqualTo(passiveDiscoveryDTO.getToggle())
            .jsonPath("$.data.passiveDiscovery.createTimeMsec").isEqualTo(passiveDiscoveryDTO.getCreateTimeMsec());
        verify(mockClient).getPassiveDiscovery(eq(accessToken));
        verify(mockHeaderUtil, times(1)).getAuthHeader(any(ResolutionEnvironment.class));
    }

    private String createPayload(String request) throws JSONException {
        return new JSONObject().put("query", request).toString();
    }
}
