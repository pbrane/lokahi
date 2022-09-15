package org.opennms.netmgt.provision.rpc.ignite.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.ignite.client.IgniteClient;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.netmgt.provision.DetectorRequestExecutorBuilder;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;

@Getter
@Setter
@Deprecated
public class IgniteLocationAwareDetectorClient implements LocationAwareDetectorClient {

    private IgniteClient igniteClient;
    private IgniteRemoteAsyncManager igniteRemoteAsyncManager;
    private DetectorRequestRouteManager detectorRequestRouteManager;

    @Override
    public DetectorRequestExecutorBuilder detect() {
        return new IgniteDetectorRequestExecutorBuilder(igniteClient, igniteRemoteAsyncManager, detectorRequestRouteManager);
    }
}
