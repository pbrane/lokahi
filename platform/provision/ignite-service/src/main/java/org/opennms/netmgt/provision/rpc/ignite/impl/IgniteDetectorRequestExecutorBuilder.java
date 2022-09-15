package org.opennms.netmgt.provision.rpc.ignite.impl;

import io.opentracing.Span;
import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.netmgt.provision.DetectorRequestExecutor;
import org.opennms.netmgt.provision.DetectorRequestExecutorBuilder;
import org.opennms.netmgt.provision.PreDetectCallback;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class IgniteDetectorRequestExecutorBuilder implements DetectorRequestExecutorBuilder {

    private final IgniteClient igniteClient;
    private final IgniteRemoteAsyncManager igniteRemoteAsyncManager;
    private final DetectorRequestRouteManager detectorRequestRouteManager;

    private String location;
    private String systemId;
    private String serviceName;
    private String detectorName;
    private InetAddress address;
    private Map<String, String> attributes = new HashMap<>();
    private Integer nodeId;
    private Span span;
    private PreDetectCallback preDetectCallback;

    public IgniteDetectorRequestExecutorBuilder(
        IgniteClient igniteClient,
        IgniteRemoteAsyncManager igniteRemoteAsyncManager,
        DetectorRequestRouteManager detectorRequestRouteManager) {

        this.igniteClient = igniteClient;
        this.igniteRemoteAsyncManager = igniteRemoteAsyncManager;
        this.detectorRequestRouteManager = detectorRequestRouteManager;
    }

    @Override
    public DetectorRequestExecutorBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    // WARNING: className is no longer used; this is replaced by detector name.  For backward API compatibility, the
    //  className is treated as detectorName (for now).
    @Override
    @Deprecated
    public DetectorRequestExecutorBuilder withClassName(String className) {
        this.detectorName = className;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withDetectorName(String detectorName) {
        this.detectorName = detectorName;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withAddress(InetAddress address) {
        this.address = address;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withAttribute(String key, String value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withNodeId(Integer nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withParentSpan(Span span) {
        this.span = span;
        return this;
    }

    @Override
    public DetectorRequestExecutorBuilder withPreDetectCallback(PreDetectCallback preDetectCallback) {
        this.preDetectCallback = preDetectCallback;
        return this;
    }

    @Override
    public DetectorRequestExecutor build() {
        return
            new IgniteDetectorRequestExecutor(
                igniteClient,
                location,
                systemId,
                serviceName,
                detectorName,
                address,
                attributes,
                nodeId,
                span,
                preDetectCallback,
                igniteRemoteAsyncManager,
                detectorRequestRouteManager
                );
    }
}
