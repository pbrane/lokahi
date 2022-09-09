package org.opennms.horizon.shared.ignite.remoteasync.client.internal;

import org.apache.ignite.client.IgniteClient;
import org.opennms.horizon.shared.ignite.remoteasync.RequestDispatcher;
import org.opennms.horizon.shared.ignite.remoteasync.client.RequestDispatcherFactory;

public class IgniteRequestDispatcherFactory implements RequestDispatcherFactory {

    @Override
    public RequestDispatcher createRequestDispatcher(IgniteClient client) {
        return client.services().serviceProxy(RequestDispatcher.class.getName(), RequestDispatcher.class, 30_000);
    }
}
