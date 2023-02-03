package org.opennms.horizon.inventory.repository;

import java.util.List;
import org.opennms.horizon.inventory.model.SyntheticTestConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyntheticTestConfigRepository extends JpaRepository<SyntheticTestConfig, Long> {
    List<SyntheticTestConfig> findByTenantIdAndSyntheticTestId(String tenant, long syntheticTestId);

}
