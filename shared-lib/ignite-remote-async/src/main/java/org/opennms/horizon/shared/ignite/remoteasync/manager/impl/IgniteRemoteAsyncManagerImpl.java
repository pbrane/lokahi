package org.opennms.horizon.shared.ignite.remoteasync.manager.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteOutClosure;
import org.opennms.horizon.shared.ignite.remoteasync.IgniteRemoteAsyncConstants;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.horizon.shared.ignite.remoteasync.manager.model.RemoteOperationResult;
import org.opennms.horizon.shared.ignite.remoteasync.compute.RemoteAsyncOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// TBD888: timeouts
public class IgniteRemoteAsyncManagerImpl implements IgniteRemoteAsyncManager {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(IgniteRemoteAsyncManagerImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final Ignite ignite;
    private final Map<Long, RemoteExecutionDetails<?>> activeRequests = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final IgniteBiPredicate<UUID, RemoteOperationResult<?>> myResponseListener = this::responseMessageListener;

    private boolean shutdownInd = false;

    public IgniteRemoteAsyncManagerImpl(Ignite ignite) {
        this.ignite = ignite;
    }

//========================================
// Lifecycle
//----------------------------------------

    public void start() {
        log.info("START listening for async remote responses");
        ignite.message().localListen(IgniteRemoteAsyncConstants.RESPONSE_TOPIC, myResponseListener);
    }

    public void shutdown() {
        log.info("STOP listening for async remote responses");
        shutdownInd = true;
        ignite.message().stopLocalListen(IgniteRemoteAsyncConstants.RESPONSE_TOPIC, myResponseListener);
    }

//========================================
// Operations
//----------------------------------------

    // TBD888: on TTL expiration, completedExceptionally()
    // 1. send a closure over ignite: ignite.compute(effectiveTargets).apply(remoteOperationWrapper, (Void) null);
    // 2. send back the response using ignite messaging: ignite.message(target).send(IgniteRemoteAsyncConstants.RESPONSE_TOPIC, remoteOperationResult);
    // 3. listen for the completion on a queue

    // NEED TO GET DONE:
    //  1. verify the async handling and job execution work
    //  2. timeouts (solve the question of where to get the timeout)
    //  3. tie back into the Detector as a new LocationAwareDetectorClient impl for ignite
    @Override
    public <T> CompletableFuture<T> submit(ClusterGroup candidateTargets, IgniteOutClosure<CompletableFuture<T>> remoteOperation) {
        //
        // Prepare the resulting future and the tracking structure.
        //
        CompletableFuture<T> future = new CompletableFuture<>();
        RemoteExecutionDetails<T> remoteExecutionDetails = new RemoteExecutionDetails<>();
        remoteExecutionDetails.setFuture(future);

        // Default the target nodes to the entire cluster
        ClusterGroup effectiveTargets = candidateTargets;
        if (effectiveTargets == null) {
            // Default to targeting any node in the cluster
            effectiveTargets = ignite.cluster();
        }

        // Allocate an ID for the request; it only needs to be unique on this node
        long id = idGenerator.incrementAndGet();

        log.debug("Submitting remote operation: id={}", id);

        // Track the operation; start the tracking before submitting the operation to ignite to avoid a possible race
        //  condition in case the remote operation completes quickly.
        activeRequests.put(id, remoteExecutionDetails);

        // Prepare the wrapper, which is the ignite closure submitted to start the async operation on the remote node
        RemoteAsyncOperation<T> remoteOperationWrapper = prepareWrapper(remoteOperation, id);

        // Send the remote operation.  Using an IgniteClosure with a Void parameter type because the API doesn't
        //  take an IgniteOutClosure.  Perhaps there's a simpler method to use?
        UUID uuid = ignite.compute(effectiveTargets).apply(remoteOperationWrapper, (Void) null);

        if (uuid != null) {
            log.debug("Remote operation started on node {}", uuid);
            remoteExecutionDetails.setRemoteNode(uuid);
        } else {
            log.warn("Remote operation failed");
        }

        return future;
    }

//========================================
// Internals
//----------------------------------------

    private <T> RemoteAsyncOperation<T> prepareWrapper(IgniteOutClosure<CompletableFuture<T>> remoteOperation, long id) {
        RemoteAsyncOperation<T> remoteOperationWrapper = new RemoteAsyncOperation<>();
        remoteOperationWrapper.setIgnite(ignite);
        remoteOperationWrapper.setRemoteOperation(remoteOperation);
        remoteOperationWrapper.setId(id);
        remoteOperationWrapper.setRequestingNode(ignite.cluster().localNode().id());

        return remoteOperationWrapper;
    }

    private boolean responseMessageListener(UUID uuid, RemoteOperationResult<?> result) {
        log.debug("RESPONSE message received: uuid={}", uuid);

        @SuppressWarnings("rawtypes")
        RemoteExecutionDetails executionDetails = activeRequests.remove(result.getId());

        if (executionDetails != null) {
            if (result.getThrowable() == null) {
                executionDetails.getFuture().complete(result.getResult());
            } else {
                executionDetails.getFuture().completeExceptionally(result.getThrowable());
            }
        }

        return shutdownInd;
    }
}
