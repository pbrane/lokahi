package org.opennms.miniongateway.detector.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.resources.SpringResource;
import org.opennms.horizon.shared.ignite.remoteasync.manager.model.RemoteOperation;
import org.opennms.miniongateway.detector.api.LocalDetectorAdapter;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The RemoteOperation instance that will be serialized/deserialized by Ignite and executed on the remote end.
 *
 * Note this is a very thin implementation with loose coupling to the server internals.  This is critical!
 */
@Slf4j
public class IgniteDetectorRemoteOperation implements RemoteOperation<Boolean> {
    @SpringResource(resourceName = "localDetectorAdapter")
    private transient LocalDetectorAdapter localDetectorAdapter;

    @Getter @Setter private String location;
    @Getter @Setter private String systemId;
    @Getter @Setter private String serviceName;
    @Getter @Setter private String detectorName;
    @Getter @Setter private InetAddress address;
    @Getter @Setter private Map<String, String> attributes; // TODO: byte[] or string-of-json?
    @Getter @Setter private Integer nodeId;
    // @Getter @Setter private Span span;

    @Override
    public CompletableFuture<Boolean> apply() {
//        return localDetectorAdapter.detect(location, systemId, serviceName, detectorName, address, nodeId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            log.info("################## CompletableFuture test! returning true");
            return true;
        });
    }
}
