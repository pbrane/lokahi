package org.opennms.horizon.inventory.repository;

import org.opennms.horizon.inventory.model.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, Long> {
    List<MonitoredService> findByTenantId(UUID tenantId);
}