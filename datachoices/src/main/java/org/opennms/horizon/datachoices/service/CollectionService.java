package org.opennms.horizon.datachoices.service;

import lombok.RequiredArgsConstructor;
import org.opennms.horizon.datachoices.service.dto.CollectionResults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectionService {

    public CollectionResults collect(String tenantId) {
        System.out.println("CollectionService.collect");
        System.out.println("tenantId = " + tenantId);

        CollectionResults results = new CollectionResults();
        results.setSystemId(tenantId);

        return results;
    }

}
