package org.opennms.horizon.it.gqlmodels;

public class NodeData {
    private Long id;
    private String nodeLabel;

    private LocationData location;

    private String monitoringLocationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }

    public String getMonitoringLocationId() {
        return monitoringLocationId;
    }

    public void setMonitoringLocationId(String monitoringLocationId) {
        this.monitoringLocationId = monitoringLocationId;
    }

}
