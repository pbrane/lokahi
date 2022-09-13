package org.opennms.miniongateway.detector.client;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.resources.SpringResource;
import org.opennms.horizon.shared.ignite.remoteasync.manager.model.RemoteOperation;
import org.opennms.miniongateway.detector.api.LocalDetectorAdapter;
import org.opennms.miniongateway.detector.api.LocalEchoAdapter;

/**
 * The RemoteOperation instance that will be serialized/deserialized by Ignite and executed on the remote end.
 *
 * Note this is a very thin implementation with loose coupling to the server internals.  This is critical!
 */
@Slf4j
@Getter
@Setter
public class IgniteEchoRemoteOperation implements RemoteOperation<Boolean> {
    @SpringResource(resourceName = "localEchoAdapter")
    @Setter(AccessLevel.NONE)
    private transient LocalEchoAdapter localEchoAdapter;

    private String location;
    private String systemId;

    //private Span span;

    @Override
    public CompletableFuture<Boolean> apply() {
        return localEchoAdapter.echo(location, systemId);
    }
}
