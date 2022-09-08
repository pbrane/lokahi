package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.UUID;

public class Request {

    private final UUID uuid;
    private final String type;
    private final Object payload;
    private final int timeToLiveMs;

    public Request(UUID uuid, String type, Object payload, int timeToLiveMs) {
        this.uuid = uuid;
        this.type = type;
        this.payload = payload;
        this.timeToLiveMs = timeToLiveMs;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public int getTimeToLiveMs() {
        return timeToLiveMs;
    }

    public Object getPayload() {
        return payload;
    }

}
