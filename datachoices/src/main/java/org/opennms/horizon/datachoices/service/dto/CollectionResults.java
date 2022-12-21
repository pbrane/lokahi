package org.opennms.horizon.datachoices.service.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CollectionResults {

    private String systemId;

    private String version;

    private long nodes;

    private long monitoredServices;

    private Map<String, Integer> deviceTypeCounts = new HashMap<>();
}
