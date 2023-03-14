package org.opennms.horizon.server.model.alerts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alert {
    private String tenantId;
    private long databaseId;
    private String uei;
    private String reductionKey;
    private String clearKey;
    private AlertType type;
    private long counter;
    private Severity severity;
    private String description;
    private String logMessage;
    private String location;
    private ManagedObject managedObject;
    private long firstEventTimeMs;
    private long lastEventId;
    private long lastUpdateTimeMs;
    private boolean isAcknowledged;
    private String ackUser;
    private long ackTimeMs;
}
