package org.opennms.horizon.server.service.grpc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opennms.dataplatform.flows.querier.ApplicationsServiceGrpc;
import org.opennms.dataplatform.flows.querier.ConversationsServiceGrpc;
import org.opennms.dataplatform.flows.querier.FlowServiceGrpc;
import org.opennms.dataplatform.flows.querier.HostsServiceGrpc;
import org.opennms.dataplatform.flows.querier.Querier;
import org.opennms.horizon.shared.constants.GrpcConstants;

import com.google.protobuf.Timestamp;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FlowClient {
    private final ManagedChannel channel;
    private final long deadlineMs;
    private FlowServiceGrpc.FlowServiceBlockingStub flowServiceStub;
    private HostsServiceGrpc.HostsServiceBlockingStub hostsServiceBlockingStub;
    private ApplicationsServiceGrpc.ApplicationsServiceBlockingStub applicationsServiceBlockingStub;
    private ConversationsServiceGrpc.ConversationsServiceBlockingStub conversationsServiceBlockingStub;

    protected void initialStubs() {
        flowServiceStub = FlowServiceGrpc.newBlockingStub(channel);
        hostsServiceBlockingStub = HostsServiceGrpc.newBlockingStub(channel);
        applicationsServiceBlockingStub = ApplicationsServiceGrpc.newBlockingStub(channel);
        conversationsServiceBlockingStub = ConversationsServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public long getNumFlows(Long hours, String hostFilter, String applicationFilter, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var flowCountRequest = Querier.GetFlowCountRequest.newBuilder()
            .addFilters(timeRangeFilter)
            .addFilters(getHostFilter(hostFilter))
            .addFilters(getApplicationFilter(applicationFilter));

        return flowServiceStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
            .getFlowCount(flowCountRequest.build())
            .getCount();
    }

    public List<Querier.TrafficSummary> getTopNHostSummaries(Long hours, String hostFilter, String applicationFilter, long N, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var hostSummariesRequest = Querier.GetTopNHostSummariesRequest.newBuilder()
            .addFilters(timeRangeFilter)
            .addFilters(getHostFilter(hostFilter))
            .addFilters(getApplicationFilter(applicationFilter))
            .setCount(N);
        return hostsServiceBlockingStub.getTopNHostSummaries(hostSummariesRequest.build()).getSummariesList();
    }

    public List<Querier.FlowingPoint> getTopNHostSeries(Long hours, String hostFilter, String applicationFilter, long N, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var hostSeriesRequest = Querier.GetTopNHostSeriesRequest.newBuilder()
            .addFilter(timeRangeFilter)
            .addFilter(getHostFilter(hostFilter))
            .addFilter(getApplicationFilter(applicationFilter))
            .setCount(N);
        return hostsServiceBlockingStub.getTopNHostSeries(hostSeriesRequest.build()).getPointsList();
    }

    public List<Querier.TrafficSummary> getTopNApplicationSummaries(Long hours, String hostFilter, String applicationFilter, long N, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var appSummariesRequest = Querier.GetTopNApplicationSummariesRequest.newBuilder()
            .addFilters(timeRangeFilter)
            .addFilters(getHostFilter(hostFilter))
            .addFilters(getApplicationFilter(applicationFilter))
            .setCount(N);
        return applicationsServiceBlockingStub.getTopNApplicationSummaries(appSummariesRequest.build()).getSummariesList();
    }

    public List<Querier.FlowingPoint> getTopNApplicationSeries(Long hours, String hostFilter, String applicationFilter, long N, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var appSeriesRequest = Querier.GetTopNApplicationSeriesRequest.newBuilder()
            .addFilter(timeRangeFilter)
            .addFilter(getHostFilter(hostFilter))
            .addFilter(getApplicationFilter(applicationFilter))
            .setCount(N);
        return applicationsServiceBlockingStub.getTopNApplicationSeries(appSeriesRequest.build()).getPointList();
    }

    public List<Querier.TrafficSummary> getTopNConversationSummaries(Long hours, String hostFilter, String applicationFilter, long N, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var convoSummariesRequest = Querier.GetTopNConversationSummariesRequest.newBuilder()
            .addFilters(timeRangeFilter)
            .addFilters(getHostFilter(hostFilter))
            .addFilters(getApplicationFilter(applicationFilter))
            .setCount(N);
        return conversationsServiceBlockingStub.getTopNConversationSummaries(convoSummariesRequest.build()).getSummariesList();
    }

    public List<Querier.FlowingPoint> getTopNConversationSeries(Long hours, String hostFilter, String applicationFilter, long N, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        Querier.Filter timeRangeFilter = getTimeRangeFilter(hours);
        final var convSeriesRequest = Querier.GetTopNConversationSeriesRequest.newBuilder()
            .addFilter(timeRangeFilter)
            .addFilter(getHostFilter(hostFilter))
            .addFilter(getApplicationFilter(applicationFilter))
            .setCount(N);
        return conversationsServiceBlockingStub.getTopNConversationSeries(convSeriesRequest.build()).getPointsList();
    }

    private Querier.Filter getTimeRangeFilter(Long hours) {
        long effectiveHours = hours != null ? hours : 24;

        Instant nowTime = Instant.now();
        Timestamp nowTimestamp = Timestamp.newBuilder()
            .setSeconds(nowTime.getEpochSecond())
            .setNanos(nowTime.getNano()).build();

        Instant thenTime = nowTime.minus(effectiveHours, ChronoUnit.HOURS);
        Timestamp thenTimestamp = Timestamp.newBuilder()
            .setSeconds(thenTime.getEpochSecond())
            .setNanos(thenTime.getNano()).build();

        return Querier.Filter.newBuilder().setTimeRange(Querier.TimeRangeFilter.newBuilder()
                .setStartTime(thenTimestamp)
                .setEndTime(nowTimestamp))
            .build();
    }

    private Querier.Filter getHostFilter(final String hostFilter) {
        if (hostFilter == null) {
            return null;
        }

        return Querier.Filter.newBuilder()
            .setHost(Querier.HostFilter.newBuilder().setIp(hostFilter))
            .build();
    }

    private Querier.Filter getApplicationFilter(final String applicationFilter) {
        if (applicationFilter == null) {
            return null;
        }

        return Querier.Filter.newBuilder()
            .setApplication(Querier.ApplicationFilter.newBuilder().setApplication(applicationFilter))
            .build();
    }
}
