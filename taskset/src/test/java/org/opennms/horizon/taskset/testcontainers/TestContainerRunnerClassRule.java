/*
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
 */

package org.opennms.horizon.taskset.testcontainers;

import java.time.Duration;
import java.util.Optional;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("rawtypes")
public class TestContainerRunnerClassRule extends ExternalResource {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TestContainerRunnerClassRule.class);

    private Logger LOG = DEFAULT_LOGGER;

    private final String dockerImage = Optional.ofNullable(System.getProperty("application.docker.image"))
        .orElse("opennms/horizon-stream-taskset:local");

    private GenericContainer applicationContainer;

    private Network network;

    public TestContainerRunnerClassRule() {
        applicationContainer = new GenericContainer(DockerImageName.parse(dockerImage).toString());
    }

    @Override
    protected void before() throws Throwable {
        network =
            Network.newNetwork()
            ;

        LOG.info("USING TEST DOCKER NETWORK {}", network.getId());

        startApplicationContainer();
    }

    @Override
    protected void after() {
        applicationContainer.stop();
    }

//========================================
// Container Startups
//----------------------------------------

    private void startApplicationContainer() {
        applicationContainer
            .withNetwork(network)
            .withNetworkAliases("taskset")
            .withExposedPorts(8990, 5005)
            .withStartupTimeout(Duration.ofMinutes(5))
            .withEnv("JAVA_TOOL_OPTIONS", "-Djava.security.egd=file:/dev/./urandom -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
            .withLogConsumer(new Slf4jLogConsumer(LOG).withPrefix("TASKSET"))
            ;

        // DEBUGGING: uncomment to force local port 5005
        //applicationContainer.getPortBindings().add("5005:5005");
        applicationContainer.start();

        var tasksetGrpcPort = applicationContainer.getMappedPort(8990); // taskset-grpc-port
        var debuggerPort = applicationContainer.getMappedPort(5005);

        LOG.info("APPLICATION MAPPED PORTS: taskset-grpc={}; debugger={}", tasksetGrpcPort, debuggerPort);

        System.setProperty("taskset-grpc-port", String.valueOf(tasksetGrpcPort));
    }
}
