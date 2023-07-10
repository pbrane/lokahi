package org.opennms.horizon.it.gqlmodels;

import java.util.List;

public class PolicyRuleData {

    private String componentType;
    private String name;
    private String tenantId;
    private List<TriggerEventData> triggerEvents; //replace with List ?

    public String getComponentType() {
        return componentType;
    }
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<TriggerEventData> getTriggerEvents() {
        return triggerEvents;
    }
    public void setTriggerEvents(List<TriggerEventData> triggerEvents) {
        this.triggerEvents = triggerEvents;
    }
}
