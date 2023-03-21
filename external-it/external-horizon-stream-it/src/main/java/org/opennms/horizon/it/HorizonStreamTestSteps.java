package org.opennms.horizon.it;

import io.cucumber.java.en.Given;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonStreamTestSteps {

    private static final Logger log = LoggerFactory.getLogger(HorizonStreamTestSteps.class);

    private final KeycloakTestSteps keycloakTestSteps;
    private final InventoryTestSteps inventoryTestSteps;
    private final MetricsTestSteps metricsTestSteps;
    private final DiscoveryTest discoveryTestSteps;

    private String ingressBaseUrl;

//========================================
// Constructor
//----------------------------------------

    public HorizonStreamTestSteps(KeycloakTestSteps keycloakTestSteps, InventoryTestSteps inventoryTestSteps, MetricsTestSteps metricsTestSteps, DiscoveryTest discoveryTestSteps) {
        this.keycloakTestSteps = keycloakTestSteps;
        this.inventoryTestSteps = inventoryTestSteps;
        this.metricsTestSteps = metricsTestSteps;
        this.discoveryTestSteps = discoveryTestSteps;

        this.inventoryTestSteps.setUserAccessTokenSupplier(this.keycloakTestSteps::getKeycloakAccessToken);
        this.inventoryTestSteps.setIngressUrlSupplier(this::getIngressBaseUrl);

        this.discoveryTestSteps.setUserAccessTokenSupplier(this.keycloakTestSteps::getKeycloakAccessToken);
        this.discoveryTestSteps.setIngressUrlSupplier(this::getIngressBaseUrl);

        this.metricsTestSteps.setUserAccessTokenSupplier(this.keycloakTestSteps::getKeycloakAccessToken);
        this.metricsTestSteps.setIngressUrlSupplier(this::getIngressBaseUrl);
        this.metricsTestSteps.setMinionsAtLocationSupplier(this.inventoryTestSteps::getMinionsAtLocation);
    }

//========================================
// Getters and Setters
//----------------------------------------

    public String getIngressBaseUrl() {
        return ingressBaseUrl;
    }


//========================================
// Test Step Definitions
//----------------------------------------

    @Given("Ingress base url in environment variable {string}")
    public void horizonStreamServerBaseUrlInEnvironmentVariable(String variableName) {
        ingressBaseUrl = System.getenv(variableName);

        log.info("INGRESS BASE URL: {}", ingressBaseUrl);
    }
}
