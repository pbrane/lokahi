///*******************************************************************************
// * This file is part of OpenNMS(R).
// *
// * Copyright (C) 2022 The OpenNMS Group, Inc.
// * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
// *
// * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
// *
// * OpenNMS(R) is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published
// * by the Free Software Foundation, either version 3 of the License,
// * or (at your option) any later version.
// *
// * OpenNMS(R) is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with OpenNMS(R).  If not, see:
// *      http://www.gnu.org/licenses/
// *
// * For more information contact:
// *     OpenNMS(R) Licensing <license@opennms.org>
// *     http://www.opennms.org/
// *     http://www.opennms.com/
// *******************************************************************************/
//
//package org.opennms.horizon.server.service;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.opennms.horizon.alerts.proto.Alert;
//import org.opennms.horizon.server.RestServerApplication;
//import org.opennms.horizon.server.config.DataLoaderFactory;
//import org.opennms.horizon.server.service.grpc.AlertsClient;
//import org.opennms.horizon.server.service.metrics.TSDBMetricsService;
//import org.opennms.horizon.server.utils.ServerHeaderUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import io.leangen.graphql.execution.ResolutionEnvironment;
//
////This purpose of this test class is keep checking the dataloader logic is correct.
//@SpringBootTest(webEnvironment = RANDOM_PORT, classes = RestServerApplication.class)
//public class GraphQLAlertsServiceTest {
//    private static final String GRAPHQL_PATH="/graphql";
//    @MockBean
//    private AlertsClient mockClient;
//    @Autowired
//    private WebTestClient webClient;
//    @MockBean
//    private ServerHeaderUtil mockHeaderUtil;
//    @MockBean
//    private TSDBMetricsService tsdbMetricsService;
//    private final String accessToken = "test-token-12345";
//    private Alert alerts1, alerts2;
//    @Captor
//    private ArgumentCaptor<List<DataLoaderFactory.Key>> keyCaptor;
//
//    @BeforeEach
//    public void setUp() {
//        alerts1 = Alert.newBuilder().build();
//        alerts2 = Alert.newBuilder().build();
//
////        IpInterfaceDTO ipInterfaceDTO = IpInterfaceDTO.newBuilder().setIpAddress("127.0.0.1").build();
////        nodeDTO4 = NodeDTO.newBuilder().setId(4L).setMonitoringLocationId(alerts2.getId()).addIpInterfaces(ipInterfaceDTO).build();
//
//        doReturn(accessToken).when(mockHeaderUtil).getAuthHeader(any(ResolutionEnvironment.class));
//    }
//
//    @AfterEach
//    public void afterTest(){
//        verifyNoMoreInteractions(mockClient);
//        verifyNoMoreInteractions(mockHeaderUtil);
//    }
//
//    @Test
//    public void testFindAllAlerts() throws JSONException {
//        doReturn(Arrays.asList(alerts1, alerts2)).when(mockClient).listAlerts(accessToken);
////        doReturn(Arrays.asList(alerts1, alerts2)).when(mockClient).listLocationsByIds(keyCaptor.capture());
//        String request = "query {findAllALerts {}}";
//        webClient.post()
//            .uri(GRAPHQL_PATH)
//            .accept(MediaType.APPLICATION_JSON)
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(createPayload(request))
//            .exchange()
//            .expectStatus().isOk()
//            .expectBody()
//            .jsonPath("$.data.findALlAlerts.size()").isEqualTo(3)
//            .jsonPath("$.data.findALlAlerts[0].location.location").isEqualTo(alerts1.getLocation())
//            .jsonPath("$.data.findALlAlerts[1].location.location").isEqualTo(alerts1.getLocation())
//            .jsonPath("$.data.findALlAlerts[2].location.location").isEqualTo(alerts2.getLocation());
//        verify(mockClient).listAlerts(accessToken);
//        verify(mockHeaderUtil, times(4)).getAuthHeader(any(ResolutionEnvironment.class));
////        verify(mockClient).listLocationsByIds(keyCaptor.capture());
////        List<DataLoaderFactory.Key> argus = keyCaptor.getValue();
////        assertThat(argus.size()).isEqualTo(2);
//    }
//
//    private String createPayload(String request) throws JSONException {
//        return new JSONObject().put("query", request).toString();
//    }
//
//}
