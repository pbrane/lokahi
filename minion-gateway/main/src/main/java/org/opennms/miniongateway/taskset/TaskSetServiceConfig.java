package org.opennms.miniongateway.taskset;

import org.apache.ignite.Ignite;
import org.opennms.taskset.service.api.TaskSetForwarder;
import org.opennms.taskset.service.api.TaskSetPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskSetServiceConfig {

    @Bean({"taskSetPublisher", "taskSetForwarder"})
    public TaskSetPublisher taskSetPublisher(Ignite ignite) {
        return new TaskSetPublisherImpl(ignite);
    }

    @Bean("taskSetIgniteReceiverService")
    public TaskSetIgniteReceiverService
    taskSetIgniteReceiverService(
        @Autowired Ignite ignite,
        @Autowired TaskSetPublisher taskSetPublisher
    ) {
        return new TaskSetIgniteReceiverService(ignite, taskSetPublisher);
    }

    @Bean(initMethod = "start")
    public TaskSetIgniteReceiverServiceLifecycleManager
    taskSetIgniteReceiverServiceLifecycleManager(
        @Autowired Ignite ignite,
        @Autowired TaskSetIgniteReceiverService taskSetIgniteReceiverService
    ) {
        return new TaskSetIgniteReceiverServiceLifecycleManager(ignite, taskSetIgniteReceiverService);
    }
}
