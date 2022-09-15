package org.opennms.netmgt.provision.rpc.ignite.impl;

import io.opentracing.Span;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.ignite.client.IgniteClient;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterIgniteService;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.netmgt.provision.DetectorRequestExecutor;
import org.opennms.netmgt.provision.PreDetectCallback;

@Deprecated
public class IgniteDetectorRequestExecutor implements DetectorRequestExecutor {

    private final IgniteClient igniteClient;
    private final String location;
    private final String systemId;
    private final String serviceName;
    private final String detectorName;
    private final InetAddress address;
    private final Map<String, String> attributes;
    private final Integer nodeId;
    private final Span span; // TODO: wire into the remote call
    private final PreDetectCallback preDetectCallback; // TODO: what does this do?  If needed, wire it across ignite

    private final IgniteRemoteAsyncManager igniteRemoteAsyncManager;
    private final DetectorRequestRouteManager detectorRequestRouteManager;

    public IgniteDetectorRequestExecutor(
        IgniteClient igniteClient,
        String location,
        String systemId,
        String serviceName,
        String detectorName,
        InetAddress address,
        Map<String, String> attributes,
        Integer nodeId,
        Span span,
        PreDetectCallback preDetectCallback,
        IgniteRemoteAsyncManager igniteRemoteAsyncManager,
        DetectorRequestRouteManager detectorRequestRouteManager
    ) {

        this.igniteClient = igniteClient;
        this.location = location;
        this.systemId = systemId;
        this.serviceName = serviceName;
        this.detectorName = detectorName;
        this.address = address;
        this.attributes = attributes;
        this.nodeId = nodeId;
        this.span = span;
        this.preDetectCallback = preDetectCallback;
        this.igniteRemoteAsyncManager = igniteRemoteAsyncManager;
        this.detectorRequestRouteManager = detectorRequestRouteManager;
    }

    @Override
    public CompletableFuture<Boolean> execute() {
        /*
        UUID nodeId = findNodeIdToUse();

        if (nodeId == null) {
            return CompletableFuture.failedFuture(
                new Exception("cannot (currently) reach a minion at location=" + location + ", system-id=" + systemId));
        }

        IgniteDetectorRemoteOperation remoteOperation = prepareRemoteOperation();
        ClusterGroup clusterGroup = igniteClient.cluster().forNodeId(nodeId);
        CompletableFuture future = igniteRemoteAsyncManager.submit(clusterGroup, remoteOperation);
        */

        MinionRouterIgniteService dispatcher = igniteClient.services().serviceProxy(MinionRouterIgniteService.IGNITE_SERVICE_NAME, MinionRouterIgniteService.class);
        if (systemId != null) {
            return dispatcher.sendDetectorRequestToMinionUsingId(systemId);
        }

        return dispatcher.sendDetectorRequestToMinionUsingLocation(location);
    }

    /**
     * Determine which Node ID to use for the next execution.
     *
     * @return
     */
    /*
    private UUID findNodeIdToUse() {
        // If system-id was specified, send downstream to that system only
        if (systemId != null) {
            return detectorRequestRouteManager.findNodeIdToUseForSystemId(systemId);
        } else {
            return detectorRequestRouteManager.findNodeIdToUseForLocation(location);
        }
    }

    private IgniteDetectorRemoteOperation prepareRemoteOperation() {
        IgniteDetectorRemoteOperation result = new IgniteDetectorRemoteOperation();
//        result.setLocation(location);
//        result.setSystemId(systemId);
//        result.setServiceName(serviceName);
//        result.setDetectorName(detectorName);
//        result.setAddress(address);
//        result.setAttributes(attributes);
//        result.setNodeId(nodeId);

        return result;
    }
    //*/

}
