package org.opennms.horizon.it;

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.CucumberOptions;
import io.restassured.response.Response;
import org.opennms.horizon.it.gqlmodels.AlertTimeRange;
import org.opennms.horizon.it.gqlmodels.GQLQuery;
import org.opennms.horizon.it.helper.TestsExecutionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

@CucumberOptions(  monochrome = true,
        features = "src/main/resources/org/opennms/horizon/it/alert-page.feature")
public class AlertPageTestSteps {
    private static final Logger LOG = LoggerFactory.getLogger(AlertPageTestSteps.class);
    private final TestsExecutionHelper helper;
    private JsonArray allAlerts;
    private JsonArray sortedAlerts;
    private JsonArray alertsWithClearFilters;
    private String filterBy;
    private AlertTimeRange alertTimeRange = null;
    private long idCleared = -1;
    private long idAcknowledged = -1;

    public AlertPageTestSteps(TestsExecutionHelper helper) {
        this.helper = helper;
    }

    @Then("At least {int} alerts have been triggered")
    public void fetchAlerts(int count) {

        String request = """
            query {
              findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, sortBy: "tenantId", sortAscending: true                                                                   
                            ) {
                            nextPage
                            alerts {
                                severity
                                label
                                nodeName
                                lastUpdateTimeMs
                            }
                          }
                        }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray alerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");
        assertTrue("Less than " + count + " were fetched.", count <= alerts.size());

    }


    @Given("I am on the Alerts page")
    public void onTheAlertsPage() {
        String request = """
            query {
              findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, sortBy: "tenantId", sortAscending: true                                                                   
                            ) {
                            nextPage
                            alerts {
                                databaseId
                                severity
                                acknowledged
                                label
                                nodeName
                                lastUpdateTimeMs
                            }
                          }
                        }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();
        allAlerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");

    }

    @When("I view the Alert list")
    public void viewTheAlertList() {
        System.out.print(allAlerts);
    }

    @And("I sort the list by {string}")
    public void sortTheListBy(String sortBy) {
        String request = """
            query {
              findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, sortBy: \"""" + sortBy + "\", sortAscending: true\n" + """
                ) {
                nextPage
                alerts {
                    severity
                    label
                    nodeName
                }
              }
            }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();

        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();
        sortedAlerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");

    }

    @Then("I will see a list filtered by Alerts with an Active state ordered by {string}")
    public void listOrderedBy(String sortBy) {

        ArrayList<String> alertList = new ArrayList<>();

        for (JsonElement element : sortedAlerts) {
            JsonObject alert = (JsonObject) element;
            alertList.add(alert.get("severity").getAsString());
        }

        LOG.info("Alerts sorted by {}: {}", sortBy, alertList);
        String[] alertArray = alertList.toArray(new String[0]);
        Arrays.sort(alertArray);
        assertTrue("Alerts were not ordered by " + sortBy, alertList.equals(Arrays.asList(alertArray)));

    }

    @And("I pick a time window of {string}")
    public void pickATimeWindow(String timeRange) {
        LOG.info("pick a time window {}", timeRange);
        alertTimeRange = switch (timeRange) {
            case "ALL" -> AlertTimeRange.ALL;
            case "LAST_24_HOURS" -> AlertTimeRange.LAST_24_HOURS;
            case "SEVEN_DAYS" -> AlertTimeRange.SEVEN_DAYS;
            case "TODAY" -> AlertTimeRange.TODAY;
            default -> null;
        };
    }

    @Then("I will see a list filtered by Alerts that occurred in the time window")
    public void listFilteredByAlertsInTheTimeWindow() {
        String request = """
            query {
              findAllAlerts(pageSize: 20, page: 0, timeRange: """ + alertTimeRange + ",\n" + """
                 sortBy: "tenantId", sortAscending: true                                                                   
                ) {
                nextPage
                alerts {
                    severity
                    label
                    nodeName
                    lastUpdateTimeMs
                }
              }
            }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - alerts occurred in {} {} ", alertTimeRange, gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray alerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");
        long currentTime = System.currentTimeMillis();
        for(JsonElement element : alerts) {
            JsonObject alert = (JsonObject) element;
            assertTrue("The event time is not in the range of " + alertTimeRange, isTimeInTimeRange(currentTime, alert.get("lastUpdateTimeMs").getAsLong()));
        };
    }
    private boolean isTimeInTimeRange(long currentTimeMillis, long eventTimeMillis) {
        Instant currentTime = Instant.ofEpochMilli(currentTimeMillis);
        Instant eventTime = Instant.ofEpochMilli(eventTimeMillis);
        Instant start = switch (alertTimeRange) {
            case TODAY -> eventTime.truncatedTo(ChronoUnit.DAYS);
            case LAST_24_HOURS -> currentTime.minus(24, ChronoUnit.HOURS);
            case SEVEN_DAYS -> currentTime.minus(7, ChronoUnit.DAYS);
            default -> null;
        };
        return !eventTime.isBefore(start);
    }

    @And("I have a filter toggled for alerts with severity {string}")
    public void listFilterBySeverity(String filterBy) throws Exception {
        this.filterBy = filterBy;
        String request = """
            query {
                  findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, severities: [\"""" + filterBy + "\"], sortBy: \"severity\", sortAscending: true\n" + """            
                            ) {
                            nextPage
                            alerts {
                               databaseId
                               severity
                            }
                          }
                  }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray alerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");

        for(JsonElement element : alerts) {
            JsonObject alert = (JsonObject) element;
            if(!filterBy.equals(alert.get("severity").getAsString())) {
                fail("Alert severity verification failed." + " Expected: " + filterBy + " but got: " + alert.get("severity").getAsString());
            }
        };
    }

    @When("I choose to clear filters")
    public void clearFilters() {
        String request = """
            query {
                  findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, severities: [], sortBy: \"severity\", sortAscending: true            
                            ) {
                            nextPage
                            alerts {
                               databaseId
                               severity
                            }
                          }
                  }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        alertsWithClearFilters = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");

    }
    @Then("alerts from all severities will show")
    public void showAllAlerts() {
        boolean otherSeverities = false;
        for(JsonElement element : alertsWithClearFilters) {
            JsonObject alert = (JsonObject) element;
            if(!filterBy.equals(alert.get("severity").getAsString())) {
                otherSeverities = true;
                break;
            }
        };
        assertTrue("Clear filters failed.", otherSeverities);

    }
    @And("I have an ACTIVE alert")
    public void selectAnActiveAlert() {
        for(JsonElement element : allAlerts) {
            JsonObject alert = (JsonObject) element;
            if(!"CLEARED".equals(alert.get("severity").getAsString())) {
                idCleared = alert.get("databaseId").getAsLong();
                break;
            }
        };
        assertTrue("Failed to find an active alert", -1 != idCleared);
    }

    @When("I choose to clear the alert")
    public void clearTheAlert() {

        String clearAlert = "mutation { clearAlert( ids:[" + idCleared + "]) {alertErrorList{databaseId error} alertList{databaseId severity} }}";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(clearAlert);

        Response response = helper.executePostQuery(gqlQuery);

        assertEquals("clear alert failed: status=" + response.getStatusCode() + "; body=" + response.getBody().asString(),
            200, response.getStatusCode());

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray errors = jsonObject.getAsJsonObject("data").getAsJsonObject("clearAlert").getAsJsonArray("alertErrorList");

        assertTrue("Clear alert failed. " + errors, 0 == errors.size());

    }

    @Then("the status of the Alert will change to {string}")
    public void verifyTheStatus(String status) {
        String request = """
            query {
                  findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, severities: [\"""" + status + "\"], sortBy: \"severity\", sortAscending: true\n" + """            
                            ) {
                            nextPage
                            alerts {
                               databaseId
                            }
                          }
                  }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray alerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");

        for(JsonElement element : alerts) {
            JsonObject alert = (JsonObject) element;
            if(idCleared == alert.get("databaseId").getAsLong()) {
                return;
            }
        };

        fail("The status of the alert with id: " + idCleared + " does not change to " + status);
    }

    @And("I have an UNACKNOWLEDGED alert")
    public void selectAnUnacknoledgedAlert() {
        for(JsonElement element : allAlerts) {
            JsonObject alert = (JsonObject) element;
            if(!alert.get("acknowledged").getAsBoolean() && !"CLEARED".equals(alert.get("severity").getAsString())) {
                idAcknowledged = alert.get("databaseId").getAsLong();
                break;
            }
        };
        assertTrue("Failed to find an UNACKNOWLEDGED alert", -1 != idAcknowledged);
    }

    @When("I choose to acknowledge the alert")
    public void acknowledgeTheAlert() {

        String clearAlert = "mutation { acknowledgeAlert( ids:[" + idAcknowledged + "]) {alertErrorList{databaseId error} alertList{databaseId acknowledged severity} }}";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(clearAlert);

        Response response = helper.executePostQuery(gqlQuery);

        assertEquals("acknowledge alert failed: status=" + response.getStatusCode() + "; body=" + response.getBody().asString(),
            200, response.getStatusCode());

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray errors = jsonObject.getAsJsonObject("data").getAsJsonObject("acknowledgeAlert").getAsJsonArray("alertErrorList");

        assertTrue("acknowledge alert failed. " + errors, 0 == errors.size());

    }

    @Then("the Alert is acknowledged")
    public void verifyAcknowledgement() {
        String request = """
            query {
                  findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, sortBy: \"severity\", sortAscending: true         
                            ) {
                            nextPage
                            alerts {
                               databaseId
                               acknowledged
                            }
                          }
                  }""";

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(request);

        LOG.info("gqlQuery - all alerts {}", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);

        String jsonResp = response.getBody().print();
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        JsonArray alerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");

        for(JsonElement element : alerts) {
            JsonObject alert = (JsonObject) element;
            if(idAcknowledged == alert.get("databaseId").getAsLong() && alert.get("acknowledged").getAsBoolean()) {
                return;
            }
        };

        fail("The alert with id: " + idAcknowledged + " was not acknowledged");
    }

}

