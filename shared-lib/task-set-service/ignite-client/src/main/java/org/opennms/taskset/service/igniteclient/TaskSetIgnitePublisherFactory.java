package org.opennms.taskset.service.igniteclient;

import org.apache.ignite.Ignite;
import org.opennms.taskset.service.api.TaskSetPublisher;

public interface TaskSetIgnitePublisherFactory {
    TaskSetPublisher create(Ignite ignite);
}
