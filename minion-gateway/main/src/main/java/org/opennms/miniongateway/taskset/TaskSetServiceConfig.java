package org.opennms.miniongateway.taskset;

import org.apache.ignite.Ignite;
import org.opennms.miniongateway.taskset.service.api.TaskSetPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskSetServiceConfig {

    @Bean({"taskSetPublisher", "taskSetForwarder"})
    public TaskSetPublisher taskSetPublisher(Ignite ignite) {
        return new TaskSetPublisherImpl(ignite);
    }

}
