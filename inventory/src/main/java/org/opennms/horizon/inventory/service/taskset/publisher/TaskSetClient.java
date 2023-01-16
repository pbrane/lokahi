package org.opennms.horizon.inventory.service.taskset.publisher;

import java.util.List;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskSet;

public interface TaskSetClient {

    void publishTaskSet(String tenantId, String location, TaskSet taskSet);

    void publishNewTasks(String tenantId, String location, List<TaskDefinition> taskList);
}
