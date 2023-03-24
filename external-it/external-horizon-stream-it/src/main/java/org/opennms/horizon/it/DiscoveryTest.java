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
package org.opennms.horizon.it;

import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.opennms.horizon.it.gqlmodels.CreateNodeData;
import org.opennms.horizon.it.gqlmodels.GQLQuery;
import org.opennms.horizon.it.gqlmodels.querywrappers.CreateNodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.ws.rs.core.HttpHeaders;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.horizon.it.InventoryTestSteps.DEFAULT_HTTP_SOCKET_TIMEOUT;

public class DiscoveryTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryTest.class);


//========================================
// Variables
//----------------------------------------
    private Supplier<String> userAccessTokenSupplier;
    private Supplier<String> ingressUrlSupplier;


//========================================
// Getters and Setters
//----------------------------------------
    public Supplier<String> getUserAccessTokenSupplier() {
        return userAccessTokenSupplier;
    }

    public void setUserAccessTokenSupplier(Supplier<String> userAccessTokenSupplier) {
        this.userAccessTokenSupplier = userAccessTokenSupplier;
    }

    public Supplier<String> getIngressUrlSupplier() {
        return ingressUrlSupplier;
    }

    public void setIngressUrlSupplier(Supplier<String> ingressUrlSupplier) {
        this.ingressUrlSupplier = ingressUrlSupplier;
    }

//========================================
// Additional methods
//----------------------------------------
    private Response executePost(URL url, String accessToken, Object body) {
        RestAssuredConfig restAssuredConfig = createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
            RestAssured
                .given()
                .config(restAssuredConfig)
            ;

        Response restAssuredResponse =
            requestSpecification
                .header(HttpHeaders.AUTHORIZATION, formatAuthorizationHeader(accessToken))
                .header(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .body(body)
                .post(url)
                .thenReturn()
            ;

        return restAssuredResponse;
    }

    private String formatAuthorizationHeader(String token) {
        return "Bearer " + token;
    }

    private RestAssuredConfig createRestAssuredTestConfig() {
        return RestAssuredConfig.config()
            .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation("SSL"))
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
                .setParam("http.socket.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
            );
    }

    private URL formatIngressUrl(String path) throws MalformedURLException {
        String baseUrl = ingressUrlSupplier.get();

        return new URL(new URL(baseUrl), path);
    }

    /**
     * Method to get the first node ID from DB
     * @return Node ID as Int first in the list
     * @throws MalformedURLException
     */
    public int getFirstNodeId() throws MalformedURLException {
        URL url = formatIngressUrl("/api/graphql");
        String accessToken = userAccessTokenSupplier.get();

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(GQLQueryConstants.GET_NODE_ID);

        Response response = executePost(url, accessToken, gqlQuery);

        JsonPath jsonPathEvaluator = response.jsonPath();
        LinkedHashMap lhm = jsonPathEvaluator.get("data");
        ArrayList map = (ArrayList) lhm.get("findAllNodes");
        LinkedHashMap nodesData = (LinkedHashMap) map.get(0);
        int id = (int) nodesData.get("id");

        return id;
    }

    /**
     * Method to check the expected status of the first node during the test
     * @param expectedStatus Expected status of the node
     * @return If the status is equals tot eh expected one
     * @throws MalformedURLException
     */
    public boolean checkTheStatusOfTheFirstNode(String expectedStatus) throws MalformedURLException {
        LOG.info("checkTheStatusOfTheNode");
        URL url = formatIngressUrl("/api/graphql");
        String accessToken = userAccessTokenSupplier.get();

        String queryList = GQLQueryConstants.LIST_NODE_METRICS;

        int nodeId = getFirstNodeId();

        Map<String, Object> queryVariables = Map.of("id", nodeId);

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(queryList);
        gqlQuery.setVariables(queryVariables);

        Response response = executePost(url, accessToken, gqlQuery);

        JsonPath jsonPathEvaluator = response.jsonPath();
        LinkedHashMap lhm = jsonPathEvaluator.get("data");
        LinkedHashMap map = (LinkedHashMap) lhm.get("nodeStatus");
        String currentStatus = (String) map.get("status");
        return currentStatus.equals(expectedStatus);
    }

//========================================
// Test Step Definitions
//----------------------------------------

    @Autowired
    private ApplicationContext applicationContext;

    public static final GenericContainer<?> TARGET_CONTAINER = new GenericContainer<>(
        new ImageFromDockerfile().withDockerfileFromBuilder(builder -> builder.from("linuxserver/openssh-server")
            .build()))
        .withExposedPorts(2222)
        .withNetwork(Network.SHARED)
        .withNetworkAliases("minion");

    public static final GenericContainer<?> MINION_CONTAINER = new GenericContainer<>(
        new ImageFromDockerfile().withDockerfileFromBuilder(builder -> builder.from("opennms/horizon-stream-minion")
            .build()))
        .withExposedPorts(8990)
        .withEnv("MINION_ID", "minion-automation")
        .withEnv("MINION_LOCATION", "minion-automation")
        .withEnv("IGNITE_SERVER_ADDRESSES", "minion")
        .withEnv("MINION_GATEWAY_HOST", "host.testcontainers.internal")
        .withEnv("MINION_GATEWAY_PORT", "8990")
        .withEnv("MINION_GATEWAY_TLS", "false")
        .withEnv("USE_KUBERNETES", "false")
        .waitingFor(Wait.forLogMessage(".*Ignite node started OK.*", 1).withStartupTimeout(Duration.ofMinutes(3)))
        .withLogConsumer(new Slf4jLogConsumer(LOG).withPrefix("MINION"))
        .withNetwork(Network.SHARED)
        .withNetworkAliases("minion");

    @Test
    public void testContainerRun() {
        Testcontainers.exposeHostPorts(8990);

        //TARGET_CONTAINER.start();
        MINION_CONTAINER.start();

        String[] args = new String[0];

        try {
            SpringApplication.run(DiscoveryTest.class, args);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        System.out.println();

    }



}
