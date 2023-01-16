package org.opennms.miniongateway.taskset.service.api;

import org.opennms.taskset.contract.TaskSet;

public interface TaskSetListener {
    void onTaskSetUpdate(TaskSet taskSet);
}
