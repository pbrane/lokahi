package org.opennms.horizon.shared.ignite.remoteasync.compute;

import lombok.Getter;
import lombok.Setter;
import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.lang.IgniteOutClosure;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.horizon.shared.ignite.remoteasync.IgniteRemoteAsyncConstants;
import org.opennms.horizon.shared.ignite.remoteasync.manager.model.RemoteOperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// This is our proxy
public class RemoteAsyncOperation<T> implements IgniteClosure<Void, UUID> {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(RemoteAsyncOperation.class);

    private Logger log = DEFAULT_LOGGER;

    @IgniteInstanceResource
    @Setter
    private Ignite ignite;

    @Getter
    @Setter
    private UUID requestingNode;

    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private IgniteCallable<CompletableFuture<T>> remoteOperation;

    private transient CompletableFuture<T> future;
    private transient boolean canceled = false;


//========================================
// Operations
//----------------------------------------


    public void cancel() {
        canceled = true;
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public UUID apply(Void unused) {
        try {
            future = remoteOperation.call();

            if (future == null) {
                log.warn("remote operation returned null CompletableFuture");

                return null;
            } else if (canceled) {
                future.cancel(true);
                log.warn("remote operation canceled in-flight");

                return null;
            }

            future.whenComplete(this::handleOperationComplete);

            return ignite.cluster().localNode().id();
        } catch (Exception exc) {
            handleOperationComplete(null, exc);
            return null;
        }
    }

//========================================
// Internals
//----------------------------------------

    private void handleOperationComplete(T value, Throwable throwable) {
        ClusterGroup target = ignite.cluster().forNodeId(requestingNode);

        RemoteOperationResult<T> remoteOperationResult = new RemoteOperationResult<>(id, value, throwable);

        ignite.message(target).send(IgniteRemoteAsyncConstants.RESPONSE_TOPIC, remoteOperationResult);
    }
}
