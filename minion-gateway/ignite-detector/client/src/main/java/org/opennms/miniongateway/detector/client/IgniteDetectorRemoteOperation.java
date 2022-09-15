package org.opennms.miniongateway.detector.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
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
public class IgniteDetectorRemoteOperation implements RemoteOperation<Boolean> {
    @SpringResource(resourceName = "localDetectorAdapter")
    private transient LocalDetectorAdapter localDetectorAdapter;

    @LoggerResource
    private transient IgniteLogger logger;

    private String location;
    private String systemId;
    private String serviceName;
    private String detectorName;
    private InetAddress address;
    private Map<String, String> attributes; // TODO: byte[] or string-of-json?
    private Integer nodeId;
    //private Span span;

    @Override
    public CompletableFuture<Boolean> call() {
        logger.info("About to execute remote operation with detector adapter " + localDetectorAdapter);
        return localDetectorAdapter.detect(location, systemId, serviceName, detectorName, address, nodeId);
    }
}
