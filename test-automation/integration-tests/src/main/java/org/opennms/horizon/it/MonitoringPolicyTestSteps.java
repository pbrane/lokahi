package org.opennms.horizon.it;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.NetworkSettings;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.CucumberOptions;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.opennms.horizon.it.gqlmodels.*;
import org.opennms.horizon.it.gqlmodels.querywrappers.AddDiscoveryResult;
import org.opennms.horizon.it.gqlmodels.querywrappers.FindAllLocationsData;
import org.opennms.horizon.it.gqlmodels.querywrappers.FindAllMinionsQueryResult;
import org.opennms.horizon.it.helper.TestsExecutionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

@CucumberOptions(  monochrome = true,
        features = "src/main/resources/org/opennms/horizon/it/monitoring-policy.feature")
public class MonitoringPolicyTestSteps {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringPolicyTestSteps.class);
    private TestsExecutionHelper helper; // = new TestsExecutionHelper();

    /* minions, containers ... */
    private Map<String, GenericContainer> minions = new ConcurrentHashMap<>();

    // Runtime Data
    private String minionLocation;
    private FindAllMinionsQueryResult findAllMinionsQueryResult;
    private List<MinionData> minionsAtLocation;
    private String lastMinionQueryResultBody;

    // certificate related runtime info location -> [keystore password=pkcs12 byte sequence]
    private Map<String, Map.Entry<String, byte[]>> keystores = new ConcurrentHashMap<>();

    private MonitorPolicyInputData monitorPolicyInputData = new MonitorPolicyInputData();
    private PolicyRuleData policyRuleData = new PolicyRuleData();
    private List<TriggerEventData> triggerEvents = new ArrayList<>();

    public MonitoringPolicyTestSteps(TestsExecutionHelper helper) { this.helper = helper; }
    @Given("Tenant id {string}")
    public void tenant_id(String tenantId) {
        monitorPolicyInputData.setTenantId(tenantId);
    }
    @Given("Monitor policy name {string} and memo {string}")
    public void monitor_policy_name_and_memo(String name, String memo) {
        monitorPolicyInputData.setName(name);
        monitorPolicyInputData.setMemo(memo);
    }
    @Given("Notify by email {string}")
    public void notify_by_email(String notifyByEmail) {
        monitorPolicyInputData.setNotifyByEmail(Boolean.parseBoolean(notifyByEmail));
    }
    @Given("Policy Rule name {string} and componentType {string}")
    public void policy_rule_name_and_component_type(String name, String componentType) {
        policyRuleData.setName(name);
        policyRuleData.setComponentType(componentType);
    }

    @Given("Monitor policy tag {string}")
    public void monito_policy_tag(String tag) {
        String[] tags = {tag};
        monitorPolicyInputData.setTags(tags);
    }

    @Given("Trigger events data")
    public void trigger_events_data(io.cucumber.datatable.DataTable table) {
        table.asLists().forEach((row) -> {
            TriggerEventData teData = new TriggerEventData();
            teData.setTriggerEvent(row.get(0));
            teData.setCount(Integer.parseInt(row.get(1)));
            teData.setOvertime(Integer.parseInt(row.get(2)));
            teData.setOvertimeUnit(row.get(3));
            teData.setSeverity(row.get(4));
            teData.setClearEvent(row.get(5));
            triggerEvents.add(teData);
        });

        policyRuleData.setTriggerEvents(triggerEvents);
        monitorPolicyInputData.setRules(List.of(policyRuleData));
    }

    private int policyId;
    @Then("Create a new policy with given parameters")
    public void create_a_new_policy_with_given_parameters() {
        String queryList = "mutation createMonitorPolicy($policy: MonitorPolicyInput!) { createMonitorPolicy(policy: $policy) { id } }";

        Map<String, Object> queryVariables = Map.of("policy", monitorPolicyInputData);

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(queryList);
        gqlQuery.setVariables(queryVariables);

        LOG.info("gqlQuery", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);

        String jsonResp = response.getBody().print();

        LOG.info("response.getBody().print() {}", jsonResp);
        JsonObject jsonObject = new JsonParser().parse(jsonResp).getAsJsonObject();

        policyId = jsonObject.getAsJsonObject("data").getAsJsonObject("createMonitorPolicy").get("id").getAsInt();
        LOG.info("policyId {}", policyId);

        assertEquals(response.getStatusCode(), 200);
    }

//========================================
// Test Step Definitions
//----------------------------------------

    @Given("Location {string} is created MP")
    public void createLocation(String location) throws Exception {
        String queryList = GQLQueryConstants.CREATE_LOCATION;

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(queryList);
        gqlQuery.setVariables(Map.of("location", location));

        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);
    }

    @Given("Location {string} is removed MP")
    public void deleteLocation(String location) throws Exception {
        LocationData locationData = commonQueryLocations().getData().getFindAllLocations().stream()
                .filter(loc -> loc.getLocation().equals(location))
                .findFirst().orElse(null);

        if (locationData == null) {
            fail("Location " + location + " not found");
        }

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(GQLQueryConstants.DELETE_LOCATION);
        gqlQuery.setVariables(Map.of("id", locationData.getId()));

        Response response = helper.executePostQuery(gqlQuery);
        assertEquals(response.getStatusCode(), 200);
    }

    @Given("Location {string} does not exist MP")
    public void queryLocationDoNotExist(String location) throws MalformedURLException {
        List<LocationData> locationData = commonQueryLocations().getData().getFindAllLocations().stream()
                .filter(data -> data.getLocation().equals(location)).toList();
        assertTrue(locationData.isEmpty());
    }

    @Then("Location {string} do exist MP")
    public void queryLocationDoExist(String location) throws MalformedURLException {
        Optional<LocationData> locationData = commonQueryLocations().getData().getFindAllLocations().stream()
                .filter(data -> data.getLocation().equals(location))
                .findFirst();
        assertTrue(locationData.isPresent());
    }

    @Given("At least one Minion is running with location {string} MP")
    public void atLeastOneMinionIsRunningWithLocation(String location) {
        minionLocation = location;
    }

    @Given("No Minion running with location {string} MP")
    public void check(String location) throws MalformedURLException {
        atLeastOneMinionIsRunningWithLocation(location);
        assertFalse(checkAtLeastOneMinionAtGivenLocation());
    }

    @Then("Wait for at least one minion for the given location reported by inventory with timeout {int}ms MP")
    public void waitForAtLeastOneMinionForTheGivenLocationReportedByInventoryWithTimeoutMs(int timeout) {
        try {
            Awaitility
                    .await()
                    .atMost(timeout, TimeUnit.MILLISECONDS)
                    .ignoreExceptions()
                    .until(this::checkAtLeastOneMinionAtGivenLocation)
            ;
        } finally {
            LOG.info("LAST CHECK MINION RESPONSE BODY: {}", lastMinionQueryResultBody);
        }
    }

    @When("Request certificate for location {string} MP")
    public void requestCertificateForLocation(String location) throws MalformedURLException {
        LOG.info("Requesting certificate for location {}.", location);

        Long locationId = commonQueryLocations().getData().getFindAllLocations().stream()
                .filter(loc -> location.equals(loc.getLocation()))
                .findFirst()
                .map(LocationData::getId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown location " + location));

        String query = String.format(GQLQueryConstants.CREATE_MINION_CERTIFICATE, locationId);
        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(query);

        Response response = helper.executePostQuery(gqlQuery);

        JsonPath jsonPathEvaluator = response.jsonPath();
        LinkedHashMap<String, String> lhm = jsonPathEvaluator.get("data.getMinionCertificate");

        byte[] pkcs12 = Base64.getDecoder().decode(lhm.get("certificate"));
        String pkcs12password = lhm.get("password");
        assertTrue(pkcs12.length > 0);
        assertNotNull(pkcs12password);
        keystores.put(location, Map.entry(pkcs12password, pkcs12));
    }

    @Then("Minion {string} is started in location {string} MP")
    public void startMinion(String systemId, String location) throws IOException {
        if (!keystores.containsKey(location)) {
            fail("Could not find location " + location + " certificate");
        }

        Map.Entry<String, byte[]> certificate = keystores.get(location);
        minions.compute(systemId, (key, existing) -> {
            if (existing != null && existing.isRunning()) {
                existing.stop();
            }
            GenericContainer<?> minion = new GenericContainer<>(DockerImageName.parse(helper.getMinionImageNameSupplier().get()))
                    .withEnv("MINION_GATEWAY_HOST", helper.getMinionIngressSupplier().get())
                    .withEnv("MINION_GATEWAY_PORT", String.valueOf(helper.getMinionIngressPortSupplier().get()))
                    .withEnv("MINION_GATEWAY_TLS", String.valueOf(helper.getMinionIngressTlsEnabledSupplier().get()))
                    .withEnv("MINION_ID", systemId)
                    .withEnv("USE_KUBERNETES", "false")
                    .withEnv("GRPC_CLIENT_KEYSTORE", "/opt/karaf/minion.p12")
                    .withEnv("GRPC_CLIENT_KEYSTORE_PASSWORD", certificate.getKey())
                    .withEnv("GRPC_CLIENT_OVERRIDE_AUTHORITY", helper.getMinionIngressOverrideAuthority().get())
                    .withEnv("IGNITE_SERVER_ADDRESSES", "localhost")
                    .withNetworkAliases("minion")
                    .withNetwork(Network.SHARED)
                    .withLabel("label", key);

            File ca = helper.getMinionIngressCaCertificateSupplier().get();
            if (ca != null) {
                try {
                    minion.withCopyToContainer(Transferable.of(Files.readString(ca.toPath())), "/opt/karaf/ca.crt");
                    minion.withEnv("GRPC_CLIENT_TRUSTSTORE", "/opt/karaf/ca.crt");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read CA certificate", e);
                }
            }

            minion.withCopyToContainer(Transferable.of(certificate.getValue()), "/opt/karaf/minion.p12");
            minion.waitingFor(Wait.forLogMessage(".*Ignite node started OK.*", 1).withStartupTimeout(Duration.ofMinutes(3)));
            minion.start();

            return minion;
        });
    }
    private GenericContainer<?> snmpContainer;
    private String ipaddress;

    @When("SNMP node {string} is started in the network of minion {string}")
    public void startSNMPNode(String nodeLabel, String systemId) throws Exception {

        LOG.info("Starting node with systemId: " + systemId);

        GenericContainer<?> minion = minions.get(systemId);

        snmpContainer = new GenericContainer<>(DockerImageName.parse(helper.getNodeImageNameSupplier().get()))
                .withNetworkAliases("nodes")
                .withNetwork(minion.getNetwork())
                .withCopyFileToContainer(MountableFile.forClasspathResource("BOOT-INF/classes/snmpd/snmpd.conf"), "/etc/snmp/snmpd.conf")
                .withLabel("label", nodeLabel);

        snmpContainer.waitingFor(Wait.forLogMessage(".*SNMPD Daemon started.*", 1).withStartupTimeout(Duration.ofMinutes(3)));
        snmpContainer.start();
        NetworkSettings networkSettings = minion.getContainerInfo().getNetworkSettings();
        Map<String, ContainerNetwork> networksMap = networkSettings.getNetworks();
        ipaddress = networksMap.values().iterator().next().getIpAddress();

        LOG.info("MINION ip={}", ipaddress);

    }

    @Then("Discover {string} for snmp node {string}, location {string} is created to discover by IP with policy tag {string}")
    public void discoverSingleNodeWithDefaults(String discoveryName, String nodeName, String location, String policyTag) throws MalformedURLException {
        discoverSNMPNode(discoveryName, nodeName, location, 161, "public", policyTag);
    }

    private long snmpNodeId;
    private String snmpNodeIp;

    public void discoverSNMPNode(String discoveryName, String nodeName, String location, int port, String community, String policyTag) throws MalformedURLException {
        GenericContainer<?> node = snmpContainer;
        if (node == null) {
            throw new RuntimeException("No node matching name " + nodeName);
        }

        NetworkSettings networkSettings = snmpContainer.getContainerInfo().getNetworkSettings();
        Map<String, ContainerNetwork> networksMap = networkSettings.getNetworks();

        snmpNodeIp = networksMap.values().iterator().next().getIpAddress();

        LOG.info("SNMP node ip={}", snmpNodeIp);

        Long locationId = helper.commonQueryLocations().getData().getFindAllLocations().stream()
                .filter(loc -> location.equals(loc.getLocation()))
                .findFirst()
                .map(LocationData::getId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown location " + location));

        String ADD_DISCOVERY_QUERY_WITH_TAG =
                "mutation { createIcmpActiveDiscovery( request: { name: \"%s\", locationId: \"%s\", ipAddresses: [\"%s\"], snmpConfig: { readCommunities: [\"%s\"], ports: [%d]\n" +
                        " }, tags: [ { name: \"%s\" } ] } ) {id, name, ipAddresses, locationId, snmpConfig { readCommunities, ports } } }";

        String query = String.format(ADD_DISCOVERY_QUERY_WITH_TAG, discoveryName, locationId, snmpNodeIp, community, port, policyTag);

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(query);

        Response response = helper.executePostQuery(gqlQuery);

        assertEquals("add-discovery query failed: status=" + response.getStatusCode() + "; body=" + response.getBody().asString(),
                200, response.getStatusCode());

        AddDiscoveryResult discoveryResult = response.getBody().as(AddDiscoveryResult.class);

        snmpNodeId = discoveryResult.getData().getCreateIcmpActiveDiscovery().getId();
        LOG.info("SNMP node id={}", snmpNodeId);

        // GRAPHQL errors result in 200 http response code and a body with "errors" detail
        assertTrue("create-node errors: " + discoveryResult.getErrors(),
                (discoveryResult.getErrors() == null) || (discoveryResult.getErrors().isEmpty()));
    }

    @Then("Send a warm start trap to Minion {string}")
    public void sendWarmStartTrap(String systemId) throws Exception {
        LOG.info("Sending warm start trap from Docker to the external minion ...");
        sendTrap(systemId, "1.3.6.1.6.3.1.1.5.2");
    }

    @Given("Add monitor policy tag {string} to the SNMP node")
    public void addPolicyTagToSNMPNode(String policyTag) throws Exception {

        String queryList = "mutation addTagsToNodes($tags: TagListNodesAddInput) { addTagsToNodes(tags: $tags) { id, location } }";

        Map<String, Object> queryVariables = Map.of("policy", monitorPolicyInputData);

        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(queryList);
        gqlQuery.setVariables(queryVariables);

        LOG.info("gqlQuery", gqlQuery);
        Response response = helper.executePostQuery(gqlQuery);

        NetworkSettings networkSettings = snmpContainer.getContainerInfo().getNetworkSettings();
        Map<String, ContainerNetwork> networksMap = networkSettings.getNetworks();

        String container_ip = networksMap.values().iterator().next().getIpAddress();

        LOG.info("SNMP node ip={}", container_ip);
    }

    @Then("Send a cold start trap to Minion {string}")
    public void sendColdStartTrap(String systemId) throws Exception {
        LOG.info("Sending cold start trap from Docker to the external minion ...");
        sendTrap(systemId, "1.3.6.1.6.3.1.1.5.1");
    }

    public void sendTrap(String systemId, String oid) throws Exception {

        GenericContainer<?> minion = minions.get(systemId);

        String community = "public"; // SNMP community string
        String trapReceiver = ipaddress + ":1162"; // SNMP trap receiver address
        String value = "123456"; // Value for the trap

        String[] command = {
                "snmptrap",
                "-v", "2c",
                "-c", community,
                trapReceiver,
                "", // empty string for the trap specific arguments
                oid
        };

        Container.ExecResult sendTrapResult = snmpContainer.execInContainer(command);
        String stdout = sendTrapResult.getStdout();
        int exitCode = sendTrapResult.getExitCode();
        LOG.info("Docker sendTrapResult stdout={}, exitCode={}, sendTrapResult={}", stdout, exitCode, sendTrapResult.getStderr());
    }

    @Then("The alert has the severity set to {string}")
    public void verifyAlertSeverity(String severity) throws Exception {
        LOG.info("Waiting 10 seconds for the new alert...");
        Thread.sleep(10000);
        listAllAlerts(severity);
    }

    public void listAllAlerts(String severity) {

        String request = """
            query {
              findAllAlerts(pageSize: 20, page: 0, timeRange: ALL, severities: ["MINOR"], sortBy: "tenantId", sortAscending: true, nodeLabel: """ + " \"" + snmpNodeIp + "\"\n" +
        """
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

        JsonArray alerts = jsonObject.getAsJsonObject("data").getAsJsonObject("findAllAlerts").getAsJsonArray("alerts");
        String alertSeverity = null;
        for(JsonElement element : alerts) {
            JsonObject alert = (JsonObject) element;
            LOG.info(alert.get("nodeName").getAsString() + " : " + snmpNodeIp);
            if(alert.get("nodeName").getAsString().equals(snmpNodeIp)) {
                alertSeverity = alert.get("severity").getAsString();
            }
        };

        assertNotNull("No alerts were sent from node: " + snmpNodeIp, alertSeverity);
        assertTrue("Severity: " + severity + " was expected but got " + alertSeverity + " instead.", severity.equals(alertSeverity));

    }

    @Then("Minion {string} is stopped MP")
    public void stopMinion(String systemId) throws Exception {

        GenericContainer<?> minion = minions.get(systemId);

        if (minion != null && minion.isRunning()) {
            minion.stop();
        }
    }

    private boolean checkAtLeastOneMinionAtGivenLocation() throws MalformedURLException {
        FindAllMinionsQueryResult findAllMinionsQueryResult = commonQueryMinions();
        List<MinionData> filtered = commonFilterMinionsAtLocation(findAllMinionsQueryResult);

        LOG.debug("MINIONS for location: count={}; location={}", filtered.size(), minionLocation);

        return ( ! filtered.isEmpty() );
    }

    private FindAllLocationsData commonQueryLocations() throws MalformedURLException {
        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(GQLQueryConstants.LIST_LOCATIONS_QUERY);
        Response restAssuredResponse = helper.executePostQuery(gqlQuery);
        Assert.assertEquals(200, restAssuredResponse.getStatusCode());
        LOG.info("FindAllLocationsData {}", restAssuredResponse.getBody().print());
        return restAssuredResponse.getBody().as(FindAllLocationsData.class);
    }

    /** @noinspection rawtypes*/
    private FindAllMinionsQueryResult commonQueryMinions() throws MalformedURLException {
        GQLQuery gqlQuery = new GQLQuery();
        gqlQuery.setQuery(GQLQueryConstants.LIST_MINIONS_QUERY);

        Response restAssuredResponse = helper.executePostQuery(gqlQuery);

        lastMinionQueryResultBody = restAssuredResponse.getBody().asString();

        Assert.assertEquals(200, restAssuredResponse.getStatusCode());

        return restAssuredResponse.getBody().as(FindAllMinionsQueryResult.class);
    }

    private List<MinionData> commonFilterMinionsAtLocation(FindAllMinionsQueryResult findAllMinionsQueryResult) {
        List<MinionData> minionData = findAllMinionsQueryResult.getData().getFindAllMinions();

        List<MinionData> minionsAtLocation =
                minionData.stream()
                        .filter((md) -> Objects.equals(md.getLocation().getLocation(), minionLocation))
                        .collect(Collectors.toList())
                ;

        LOG.debug("MINIONS for location: count={}; location={}", minionsAtLocation.size(), minionLocation);

        return minionsAtLocation;
    }

}
