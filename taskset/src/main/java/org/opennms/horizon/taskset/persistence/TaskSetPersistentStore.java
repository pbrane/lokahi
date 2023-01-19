package org.opennms.horizon.taskset.persistence;

import org.opennms.taskset.contract.TaskSet;

public interface TaskSetPersistentStore {

    void store(String tenantId, String locationId, TaskSet taskSet);

    TaskSet retrieve(String tenantId, String locationId);

}
