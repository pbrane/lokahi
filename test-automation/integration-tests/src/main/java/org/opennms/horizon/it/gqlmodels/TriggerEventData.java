package org.opennms.horizon.it.gqlmodels;

public class TriggerEventData {

    private String clearEvent;
    private int count;
    private int overtime;
    private String overtimeUnit;

    private String severity;
    private String tenantId;
    private String triggerEvent;

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public int getOvertime() { return overtime; }
    public void setOvertime(int overtime) { this.overtime = overtime; }

    public String getClearEvent() {
        return clearEvent;
    }
    public void setClearEvent(String clearEvent) {
        this.clearEvent = clearEvent;
    }

    public String getOvertimeUnit() { return overtimeUnit; }
    public void setOvertimeUnit(String overtimeUnit) {
        this.overtimeUnit = overtimeUnit;
    }
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }
}
