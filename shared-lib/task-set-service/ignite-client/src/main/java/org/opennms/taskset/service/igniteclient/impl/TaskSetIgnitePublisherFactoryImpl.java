package org.opennms.taskset.service.igniteclient.impl;

import org.apache.ignite.Ignite;
import org.opennms.taskset.service.api.TaskSetPublisher;
import org.opennms.taskset.service.igniteclient.TaskSetIgnitePublisherFactory;

public class TaskSetIgnitePublisherFactoryImpl implements TaskSetIgnitePublisherFactory {
    @Override
    public TaskSetPublisher create(Ignite ignite) {
        TaskSetIgnitePublisherImpl result = new TaskSetIgnitePublisherImpl();
        result.setIgnite(ignite);

        return result;
    }
}
