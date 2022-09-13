package org.opennms.horizon.shared.ignite.remoteasync.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.horizon.shared.ignite.remoteasync.Broadcast;
import org.opennms.horizon.shared.ignite.remoteasync.Request;
import org.opennms.horizon.shared.ignite.remoteasync.RequestDispatcher;

public class IgniteRequestDispatcherFactory {

    private Ignite ignite;

    public IgniteRequestDispatcherFactory(Ignite ignite) {
        this.ignite = ignite;
    }

    public void init() {
        IgniteRequestDispatcher dispatcher = new IgniteRequestDispatcher();

        ignite.services().deployClusterSingleton(RequestDispatcher.class.getName(), dispatcher);
    }

}
