package org.opennms.horizon.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.opennms.horizon.inventory.model.SyntheticTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyntheticTransactionRepository extends JpaRepository<SyntheticTransaction, Long> {

    List<SyntheticTransaction> findByTenantId(String tenantId);

    Optional<SyntheticTransaction> findByTenantIdAndId(String tenant, Long id);

    void deleteByTenantIdAndId(String tenant, Long id);

}
