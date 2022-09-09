package org.opennms.taskset.service.igniteclient.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;
import org.opennms.taskset.model.TaskSet;
import org.opennms.taskset.service.api.TaskSetPublisher;
import org.opennms.taskset.service.model.LocatedTaskSet;

public class TaskSetIgnitePublisherImpl implements TaskSetPublisher {
    private IgniteClient igniteClient;

//========================================
// Getters and Setters
//----------------------------------------

    public void setIgniteClient(IgniteClient igniteClient) {
        this.igniteClient = igniteClient;
    }


//========================================
// Ignite Task Set Client API
//----------------------------------------

    @Override
    public void publishTaskSet(String location, TaskSet taskSet) {
        LocatedTaskSet locatedTaskSet = new LocatedTaskSet(location, taskSet);

        Publisher publisher = igniteClient.services().serviceProxy("org.opennms.horizon.shared.ignite.remoteasync.RequestDispatcher", Publisher.class);
        publisher.sendToLocation(location, locatedTaskSet);
        //igniteClient.message().send(TaskSetPublisher.TASK_SET_TOPIC, locatedTaskSet);
    }

    interface Publisher {
        void sendToLocation(String location, LocatedTaskSet taskSet);
    }

}
