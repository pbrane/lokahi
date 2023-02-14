package org.opennms.horizon.server.model.flows;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrafficSummary {
    long bytesIn;
    long bytesOut;
    String label;
}
