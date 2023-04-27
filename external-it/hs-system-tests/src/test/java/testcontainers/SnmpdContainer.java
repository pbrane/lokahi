package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.InternetProtocol;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.time.Duration;
import java.util.List;

import static org.opennms.horizon.systemtests.CucumberHooks.SNMPDS;
import static org.testcontainers.containers.Network.SHARED;

public class SnmpdContainer extends GenericContainer<SnmpdContainer> {


    public static void main(String[] args) {
        SnmpdContainer.createNewSnmpdContainer();
    }

    Network network = Network.newNetwork();

    public SnmpdContainer() {
        super("linuxserver/openssh-server");

        // expose TCP ports here
        //withExposedPorts(161);
//            .withNetworkAliases("horizon-stream")
//            .withNetwork(SHARED)
//            .withNetworkMode(network.getId())
//            .withExposedPorts(161).withCreateContainerCmdModifier(
//                cmd -> cmd.withNetworkMode(network.getId()).withHostConfig(
//                        new HostConfig()
//                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(161), new ExposedPort(161))))
//                    .withNetworkMode(network.getId())).withStartupTimeout(Duration.ofMinutes(2L));
//
//            //.waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));
//
//        // expose UDP ports here
//        this.getPortBindings().addAll(List.of(
//            ExposedPort.udp(161).toString()
//        ));

        System.out.println();
    }

    public static void createNewSnmpdContainer() {
        SnmpdContainer snmp = new SnmpdContainer();
        snmp.start();
        SNMPDS.add(snmp);
    }
}
