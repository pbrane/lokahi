package org.opennms.horizon.inventory.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class MonitoredService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitored_service_type_id", referencedColumnName = "id")
    private MonitoredServiceType monitoredServiceType;

    @Column(name = "monitored_service_type_id", insertable = false, updatable = false)
    private long monitoredServiceTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_interface_id", referencedColumnName = "id")
    private IpInterface ipInterface;

    @Column(name = "ip_interface_id", insertable = false, updatable = false)
    private long ipInterfaceId;
}
