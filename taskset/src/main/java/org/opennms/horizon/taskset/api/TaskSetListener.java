package org.opennms.horizon.taskset.api;

import org.opennms.taskset.contract.TaskSet;

public interface TaskSetListener {
    void onTaskSetUpdate(TaskSet taskSet);
}
