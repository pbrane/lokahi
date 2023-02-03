package org.opennms.horizon.inventory.model;

public interface TenantAware {

    void setId(long id);
    long getId();

    void setTenantId(String tenantId);
    String getTenantId();

}
