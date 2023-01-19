package org.opennms.horizon.taskset.gprc;

import io.grpc.stub.StreamObserver;
import org.opennms.horizon.shared.grpc.common.GrpcIpcServer;
import org.opennms.horizon.shared.grpc.common.TenantIDGrpcServerInterceptor;
import org.opennms.horizon.taskset.api.TaskSetPublisher;
import org.opennms.taskset.service.contract.PublishTaskSetRequest;
import org.opennms.taskset.service.contract.PublishTaskSetResponse;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class TaskSetGrpcService extends TaskSetServiceGrpc.TaskSetServiceImplBase {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskSetGrpcService.class);

    private Logger log = DEFAULT_LOGGER;

    private TaskSetPublisher taskSetPublisher;

    private GrpcIpcServer grpcIpcServer;

    private TenantIDGrpcServerInterceptor tenantIDGrpcServerInterceptor;

    @Override
    public void publishTaskSet(PublishTaskSetRequest request, StreamObserver<PublishTaskSetResponse> responseObserver) {
        // Retrieve the Tenant ID from the TenantID GRPC Interceptor
        String tenantId = tenantIDGrpcServerInterceptor.readCurrentContextTenantId();

        taskSetPublisher.publishTaskSet(tenantId, request.getLocation(), request.getTaskSet());

        PublishTaskSetResponse response =
            PublishTaskSetResponse.newBuilder()
                .build()
            ;

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
