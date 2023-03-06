package org.opennms.horizon.alertservice.db.repository;

import org.opennms.horizon.alertservice.db.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Alert findByReductionKey(String reductionKey);
}
