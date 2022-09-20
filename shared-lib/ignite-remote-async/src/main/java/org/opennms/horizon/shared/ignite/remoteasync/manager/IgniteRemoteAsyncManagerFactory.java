package org.opennms.horizon.shared.ignite.remoteasync.manager;

import org.apache.ignite.Ignite;

public interface IgniteRemoteAsyncManagerFactory {
    /**
     * Create a client for the given Ignite instance.  Note that only 1 client is recommended per Ignite instance.
     *
     * @param ignite the Ignite instance used to communicate with the Ignite cluster.
     * @return client that can be used to submit remote async operations.
     */
    IgniteRemoteAsyncManager create(Ignite ignite);
}
