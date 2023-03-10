package org.opennms.horizon;

import org.junit.Test;
import org.opennms.horizon.exttest.TestMain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class DiscoveryTest {

    @Autowired
    private ApplicationContext applicationContext;

    public static final GenericContainer<?> TARGET_CONTAINER = new GenericContainer<>(
        new ImageFromDockerfile().withDockerfileFromBuilder(builder -> builder.from("linuxserver/openssh-server")
            .build()))
        .withExposedPorts(2222)
        .withNetwork(Network.SHARED)
        .withNetworkAliases("target");

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
//        .withNetwork(Network.SHARED)
        .withNetworkAliases("minion");

    @Test
    public void testContainerRun() {
        Testcontainers.exposeHostPorts(8990);

        TARGET_CONTAINER.start();
        MINION_CONTAINER.start();

        String[] args = new String[0];

        try {
            SpringApplication.run(TestMain.class, args);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        System.out.println();

    }


}
