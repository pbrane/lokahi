package org.opennms.horizon.shared.ignite.remoteasync.client;

import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;
import org.opennms.horizon.shared.ignite.remoteasync.RequestDispatcher;

public interface RequestDispatcherFactory {

    RequestDispatcher createRequestDispatcher(IgniteClient client);

}
