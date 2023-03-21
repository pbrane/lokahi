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
import org.opennms.horizon.it.gqlmodels.CreateNodeData;
import org.opennms.horizon.it.gqlmodels.GQLQuery;
import org.opennms.horizon.it.gqlmodels.querywrappers.CreateNodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.net.MalformedURLException;
import java.net.URL;
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

}
