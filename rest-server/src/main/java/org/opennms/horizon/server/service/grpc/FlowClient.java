package org.opennms.horizon.server.service.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.opennms.dataplatform.flows.querier.FlowServiceGrpc;
import org.opennms.dataplatform.flows.querier.Querier;
import org.opennms.horizon.shared.constants.GrpcConstants;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FlowClient {
    private final ManagedChannel channel;
    private final long deadlineMs;
    private FlowServiceGrpc.FlowServiceBlockingStub flowServiceStub;
    protected void initialStubs() {
        flowServiceStub = FlowServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public long getNumFlows(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Instant nowTime = Instant.now();
        Timestamp nowTimestamp = Timestamp.newBuilder()
            .setSeconds(nowTime.getEpochSecond())
            .setNanos(nowTime.getNano()).build();

        Instant thenTime = nowTime.minus(24, ChronoUnit.HOURS);
        Timestamp thenTimestamp = Timestamp.newBuilder()
            .setSeconds(thenTime.getEpochSecond())
            .setNanos(thenTime.getNano()).build();

        Querier.Filter timeRangeFilter = Querier.Filter.newBuilder().setTimeRange(Querier.TimeRangeFilter.newBuilder()
            .setStartTime(thenTimestamp)
            .setEndTime(nowTimestamp))
            .build();

        Querier.GetFlowCountRequest flowCountRequest = Querier.GetFlowCountRequest.newBuilder()
            .addFilters(timeRangeFilter)
            .build();

        return flowServiceStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
            .getFlowCount(flowCountRequest)
            .getCount();
    }
}
