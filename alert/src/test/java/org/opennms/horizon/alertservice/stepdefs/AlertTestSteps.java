/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.horizon.alertservice.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.opennms.horizon.alertservice.RetryUtils;
import org.opennms.horizon.alertservice.kafkahelper.KafkaTestHelper;
import org.opennms.horizon.alertservice.model.AlertDTO;
import org.opennms.horizon.alertservice.model.AlertSeverity;
import org.opennms.horizon.alertservice.rest.AlertCollectionDTO;
import org.opennms.horizon.alertservice.rest.support.MultivaluedMapImpl;
import org.opennms.horizon.events.proto.AlertData;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventLog;
import org.opennms.horizon.shared.constants.GrpcConstants;

@Slf4j
public class AlertTestSteps {

    public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 15_000;

    //
    // Test Injectables
    //
    private RetryUtils retryUtils;

    //
    // Test Configuration
    //
    private String applicationBaseUrl;
    private String kafkaBootstrapUrl;
    private KafkaTestHelper kafkaTestHelper;

    //
    // Test Runtime Data
    //
    private Response restAssuredResponse;
    private JsonPath parsedJsonResponse;
    private Long lastAlertId;
    private String testAlertReductionKey;

//========================================
// Constructor
//----------------------------------------

    public AlertTestSteps(RetryUtils retryUtils, KafkaTestHelper kafkaTestHelper) {
        this.retryUtils = retryUtils;
        this.kafkaTestHelper = kafkaTestHelper;
    }


//========================================
// Gherkin Rules
//========================================

    @Given("Kafka topics {string} {string}")
    public void createKafkaTopic(String consumerTopicEnv, String producerTopicEnv) throws Exception {
        kafkaTestHelper.startConsumerAndProducer(consumerTopicEnv, producerTopicEnv);
    }

    @Given("Application Base URL in system property {string}")
    public void applicationBaseURLInSystemProperty(String systemProperty) throws Exception {
        this.applicationBaseUrl = System.getProperty(systemProperty);

        log.info("Using BASE URL {}", this.applicationBaseUrl);
    }

    @Given("Kafka Bootstrap URL in system property {string}")
    public void kafkaRestServerURLInSystemProperty(String systemProperty) throws Exception {
        this.kafkaBootstrapUrl = System.getProperty(systemProperty);
        this.kafkaTestHelper.setKafkaBootstrapUrl(kafkaBootstrapUrl);

        log.info("Using KAFKA BASE URL {}", this.kafkaBootstrapUrl);
    }

    @Then("Send POST request to application at path {string}")
    public void sendPOSTRequestToApplicationAtPath(String path) throws Exception {
        commonSendPOSTRequestToApplication(path);
    }

    @Then("Send POST request to clear alert at path {string}")
    public void sendPOSTRequestToClearAlertAtPath(String path) throws Exception {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        commonSendPOSTRequestToApplication(path+"/"+ alertDTO.getAlertId());
    }

    @Then("Send PUT request to add memo at path {string}")
    public void sendPUTRequestToAddMemoAtPath(String path) throws Exception {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        MultivaluedMapImpl multiValuedMap = new MultivaluedMapImpl();
        multiValuedMap.putSingle("body", "blahNobody");
        commonSendPUTRequestWithBodyToApplication(path+"/"+ alertDTO.getAlertId(), multiValuedMap);
    }

    @Then("Send POST request to acknowledge alert at path {string}")
    public void sendPOSTRequestToAckAlertAtPath(String path) throws Exception {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        commonSendPOSTRequestToApplication(path+"/"+ alertDTO.getAlertId()+"/blahUserId");
    }

    @Then("Send POST request to unacknowledge alert at path {string}")
    public void sendPOSTRequestToUnAckAlertAtPath(String path) throws Exception {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        commonSendPOSTRequestToApplication(path+"/"+ alertDTO.getAlertId());
    }

    @Then("Send POST request to set alert severity at path {string}")
    public void sendPOSTRequestToSetAlertSeverityAtPath(String path) throws Exception {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        String uri = String.format("%s/%d/%s", path, alertDTO.getAlertId(), AlertSeverity.INDETERMINATE.getLabel());
        commonSendPOSTRequestToApplication(uri);
    }

    @Then("Send POST request to escalate alert severity at path {string}")
    public void sendPOSTRequestToEscalateAlertSeverityAtPath(String path) throws Exception {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        commonSendPOSTRequestToApplication(path+"/"+ alertDTO.getAlertId());
    }

    @Then("Send Event message to Kafka at topic {string} with alert reduction key {string} with tenant {string}")
    public void sendMessageToKafkaAtTopic(String topic, String alertReductionKey, String tenantId) {
        testAlertReductionKey = alertReductionKey;

        AlertData alertData =
            AlertData.newBuilder()
                .setReductionKey(testAlertReductionKey)
                .build()
            ;

        EventLog eventLog = EventLog.newBuilder()
            .setTenantId(tenantId)
            .addEvent(Event.newBuilder()
                .setTenantId(tenantId)
                .setNodeId(10L)
                .setUei("BlahUEI")
                .setAlertData(alertData))
            .build();

        kafkaTestHelper.sendToTopic(topic, eventLog.toByteArray());
    }

    @Then("send GET request to application at path {string}, with timeout {int}ms, until JSON response matches the following JSON path expressions")
    public void sendGETRequestToApplicationAtPathUntilJSONResponseMatchesTheFollowingJSONPathExpressions(String path, int timeout, List<String> jsonPathExpressions) throws Exception {
        boolean success =
            retryUtils.retry(
                () -> this.processGetRequestThenCheckJsonPathMatch(path, jsonPathExpressions),
                result -> result,
                100,
                timeout,
                false
            );

        assertTrue("GET request expected to return JSON response matching JSON path expression(s)", success);
    }

    @Then("delay")
    public void delay() throws InterruptedException{
        Thread.sleep(10000);
    }

    @Then("Verify the HTTP response code is {int}")
    public void verifyTheHTTPResponseCodeIs(int expectedStatusCode) {
        assertEquals(expectedStatusCode, restAssuredResponse.getStatusCode());
    }

    @Then("Send GET request to application at path {string}")
    public void sendGETRequestToApplicationAtPath(String path) throws Exception {
        commonSendGetRequestToApplication(path);
    }

    @Then("Send DELETE request to application at path {string}")
    public void sendDELETERequestToApplicationAtPath(String path) throws Exception {
        log.info("####### sending POST to clear alert {}", lastAlertId);
        commonSendDELETERequestToApplication(path+"/"+lastAlertId);
    }

    @Then("Send DELETE request to remove memo at path {string}")
    public void sendDELETERequestToRemoveMemoAtPath(String path) throws Exception {
        log.info("####### sending POST to remove memo {}", lastAlertId);
        commonSendDELETERequestToApplication(path+"/"+lastAlertId);
    }

    @Then("^parse the JSON response$")
    public void parseTheJsonResponse() throws Exception {
        commonParseJsonResponse();
    }

    @Then("Verify JSON path expressions match$")
    public void verifyJsonPathExpressionsMatch(List<String> pathExpressions) throws Exception {
        for (String onePathExpression : pathExpressions) {
            verifyJsonPathExpressionMatch(parsedJsonResponse, onePathExpression);
        }
    }

    @Then("Verify alert was cleared")
    public void verifyAlertWasCleared() {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);

        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);

        assertEquals(AlertSeverity.CLEARED, alertDTO.getSeverity());
    }

    @Then("Verify alert was acknowledged")
    public void verifyAlertWasAcked() {

        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        assertNotNull(alertDTO.getAlertAckTime());
        assertNotNull(alertDTO.getAlertAckUser());
    }

    @Then("Verify alert was unacknowledged")
    public void verifyAlertWasUnAcked() {

        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        assertNull(alertDTO.getAlertAckTime());
        assertNull(alertDTO.getAlertAckUser());
    }

    @Then("Verify alert severity was escalated")
    public void verifyAlertWasEscalated() {

        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        assertTrue(alertDTO.getSeverity().isGreaterThan(AlertSeverity.INDETERMINATE));
    }

    @Then("Verify alert was uncleared")
    public void verifyAlertWasUncleared() {

        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        assertEquals(alertDTO.getLastEventSeverity(), alertDTO.getSeverity());
    }

    @Then("Verify topic {string} has {int} messages with tenant {string}")
    public void verifyTopicContainsTenant(String topic, int expectedMessages, String tenant) {
        int foundMessages = 0;

        List<ConsumerRecord<String, byte[]>> records = kafkaTestHelper.getConsumedMessages(topic);
        for (ConsumerRecord record: records) {
            Headers headers = record.headers();

            for (Header header: headers) {
                if (header.key().equals(GrpcConstants.TENANT_ID_KEY)) {
                    byte[] headerValue = header.value();
                    String tenantId = new String(headerValue);

                    if(tenant.equals(tenantId)) {
                        foundMessages++;
                    }
                    break;
                }
            }
        }

        assertEquals(expectedMessages, foundMessages);
    }

    @Then("Remember alert id")
    public void rememberAlertId() {
        AlertCollectionDTO alertCollectionDTO = restAssuredResponse.getBody().as(AlertCollectionDTO.class);
        List<AlertDTO> alertDTOList = alertCollectionDTO.getAlerts();
        AlertDTO alertDTO = findCurrentTestAlert(alertDTOList);
        this.lastAlertId = alertDTO.getAlertId();
    }

//========================================
// Utility Rules
//----------------------------------------

    @Then("^DEBUG dump the response body$")
    public void debugDumpTheResponseBody() {
        log.info("RESPONSE BODY = {}", restAssuredResponse.getBody().asString());
    }

//========================================
// Internals
//----------------------------------------

    private AlertDTO findCurrentTestAlert(List<AlertDTO> alertList) {
        return
            alertList
                .stream()
                .filter((alertDTO) -> Objects.equals(testAlertReductionKey, alertDTO.getReductionKey()))
                .findFirst()
                .orElse(null)
            ;
    }

    private void verifyJsonPathExpressionMatch(JsonPath jsonPath, String pathExpression) {
        String[] parts = pathExpression.split(" == ", 2);

        if (parts.length == 2) {
            // Expression and value to match - evaluate as a string and compare
            String actualValue = jsonPath.getString(parts[0]);
            String actualTrimmed;

            if (actualValue != null) {
                actualTrimmed = actualValue.trim();
            }  else {
                actualTrimmed = null;
            }

            String expectedTrimmed = parts[1].trim();

            assertEquals("matching to JSON path " + parts[0], expectedTrimmed, actualTrimmed);
        } else {
            // Just an expression - evaluate as a boolean
            assertTrue("verifying JSON path expression " + pathExpression, jsonPath.getBoolean(pathExpression));
        }
    }

    private void commonSendGetRequestToApplication(String path) throws MalformedURLException {
        URL requestUrl = new URL(new URL(this.applicationBaseUrl), path);

        RestAssuredConfig restAssuredConfig = this.createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
            RestAssured
                .given()
                .config(restAssuredConfig);

        restAssuredResponse =
            requestSpecification
                .get(requestUrl)
                .thenReturn()
        ;
    }
    private RestAssuredConfig createRestAssuredTestConfig() {
        return RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
                .setParam("http.socket.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
            );
    }

    private void commonSendPOSTRequestToApplication(String path) throws MalformedURLException {
        URL requestUrl = new URL(new URL(this.applicationBaseUrl), path);

        RestAssuredConfig restAssuredConfig = this.createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
            RestAssured
                .given()
                .config(restAssuredConfig);

        restAssuredResponse =
            requestSpecification
                .post(requestUrl)
                .thenReturn();
    }

    private void commonSendPUTRequestWithBodyToApplication(String path, MultivaluedMapImpl body) throws MalformedURLException {
        URL requestUrl = new URL(new URL(this.applicationBaseUrl), path);

        RestAssuredConfig restAssuredConfig = this.createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
            RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Gson().toJson(body))
                .config(restAssuredConfig);

        restAssuredResponse =
            requestSpecification
                .put(requestUrl)
                .thenReturn();
    }

    private void commonSendDELETERequestToApplication(String path) throws MalformedURLException {
        URL requestUrl = new URL(new URL(this.applicationBaseUrl), path);

        RestAssuredConfig restAssuredConfig = this.createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
            RestAssured
                .given()
                .config(restAssuredConfig);

        restAssuredResponse =
            requestSpecification
                .delete(requestUrl)
                .thenReturn();
    }

    private void commonParseJsonResponse() {
        parsedJsonResponse = JsonPath.from((this.restAssuredResponse.getBody().asString()));
    }

    private boolean processGetRequestThenCheckJsonPathMatch(String path, List<String> jsonPathExpressions) {
        log.debug("running get with check; path={}; json-path-expressions={}", path, jsonPathExpressions);
        try {
            commonSendGetRequestToApplication(path);
            commonParseJsonResponse();

            log.debug("checking json path expressions");
            for (String onePathExpression : jsonPathExpressions) {
                verifyJsonPathExpressionMatch(parsedJsonResponse, onePathExpression);
            }

            log.debug("finished get with check; path={}; json-path-expressions={}", path, jsonPathExpressions);
            return true;
        } catch (Throwable thrown) {    // Assertions extend Error
            throw new RuntimeException(thrown);
        }
    }

    private String formatKafkaRestProducerMessageBody(byte[] payload) throws JsonProcessingException {
        KafkaRestRecord record = new KafkaRestRecord();

        byte[] encoded = Base64.getEncoder().encode(payload);
        record.setValue(new String(encoded, StandardCharsets.UTF_8));

        KafkaRestProducerRequest request = new KafkaRestProducerRequest();
        request.setRecords(new KafkaRestRecord[] { record });

        return new ObjectMapper().writeValueAsString(request);
    }

    private static class KafkaRestProducerRequest {
        private KafkaRestRecord[] records;

        public KafkaRestRecord[] getRecords() {
            return records;
        }

        public void setRecords(KafkaRestRecord[] records) {
            this.records = records;
        }
    }

    private static class KafkaRestRecord {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private <K,V> KafkaProducer<K,V> createKafkaProducer() {

        log.info("####### Kafka Producer");
        // create instance for properties to access producer configs
        Properties props = new Properties();

        //Assign localhost id
        props.put("bootstrap.servers", kafkaBootstrapUrl);

        //Set acknowledgements for producer requests.
        props.put("acks","all");

        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);

        //Specify buffer size in config
        props.put("batch.size", 16384);

        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);

        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        return new KafkaProducer<K, V>(props);
    }
}