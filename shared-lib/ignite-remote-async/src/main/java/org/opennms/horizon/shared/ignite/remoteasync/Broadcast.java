package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.UUID;

public class Broadcast {

    private final UUID uuid;

    private final String type;
    private final Object payload;

    public Broadcast(UUID uuid, String type, Object payload) {
        this.uuid = uuid;
        this.type = type;
        this.payload = payload;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

}
