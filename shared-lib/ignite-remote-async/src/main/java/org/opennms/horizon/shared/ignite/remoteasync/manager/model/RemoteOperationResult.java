package org.opennms.horizon.shared.ignite.remoteasync.manager.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class RemoteOperationResult<T> implements Serializable {
    private final long id;
    private final T result;
    private final Throwable throwable;
}
