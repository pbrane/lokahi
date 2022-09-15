package org.opennms.horizon.shared.ignite.remoteasync.manager;

import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteOutClosure;

import java.util.concurrent.CompletableFuture;

// TBD888: timeouts
public interface IgniteRemoteAsyncManager {
    /**
     * Submit the given remote operation to one of the nodes in the given candidateTargets (or the entire cluster if
     *  null) and return a CompletableFuture that will be complete after the remote operation completes.
     *
     * @param candidateTargets potential nodes from which one will be chosen to execute the remote operation; if null,
     *                         any of the nodes in the entire cluster may be chosen.
     * @param remoteOperation operation to execute on the remote; this instance will be sent to another node using
     *                        java object serialization.
     * @param <T> type of the return value on completion of the future.
     * @return a future tracking the state of the remote operation.
     */
    <T> CompletableFuture<T> submit(ClusterGroup candidateTargets, IgniteCallable<CompletableFuture<T>> remoteOperation);
}
