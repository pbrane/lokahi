package org.opennms.horizon.shared.ignite.remoteasync.compute;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RoutingRequest {
    private String value;
    private RoutingType type;

    public enum RoutingType { ID, LOCATION}

}
