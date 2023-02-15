package org.opennms.horizon.server.model.flows;


import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Flows flowing through space and time
public class FlowingPoint {
    Instant timestamp;

    String direction;

    String label;

    double value;
}
