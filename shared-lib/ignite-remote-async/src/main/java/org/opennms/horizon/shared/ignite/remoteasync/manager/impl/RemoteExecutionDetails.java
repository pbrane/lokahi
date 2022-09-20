package org.opennms.horizon.shared.ignite.remoteasync.manager.impl;

import lombok.Data;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Data
public class RemoteExecutionDetails<T> {
    private CompletableFuture<T> future;
    private UUID remoteNode;
}
