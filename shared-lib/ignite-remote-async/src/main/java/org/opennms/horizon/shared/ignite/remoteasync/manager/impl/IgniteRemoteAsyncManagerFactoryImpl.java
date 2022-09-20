package org.opennms.horizon.shared.ignite.remoteasync.manager.impl;

import org.apache.ignite.Ignite;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManagerFactory;

public class IgniteRemoteAsyncManagerFactoryImpl implements IgniteRemoteAsyncManagerFactory {
    @Override
    public IgniteRemoteAsyncManager create(Ignite ignite) {
        IgniteRemoteAsyncManagerImpl result = new IgniteRemoteAsyncManagerImpl(ignite);
        result.start();

        return result;
    }
}
