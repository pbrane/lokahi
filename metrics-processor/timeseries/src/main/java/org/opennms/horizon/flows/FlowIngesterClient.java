package org.opennms.horizon.flows;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.dataplatform.flows.document.FlowDocument;
import org.opennms.dataplatform.flows.document.FlowDocumentLog;
import org.opennms.dataplatform.flows.ingester.v1.IngesterGrpc;
import org.opennms.dataplatform.flows.ingester.v1.StoreFlowDocumentRequest;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Slf4j
@RequiredArgsConstructor
public class FlowIngesterClient {
    private final ManagedChannel channel;
    private final long deadlineMs;
    private IngesterGrpc.IngesterBlockingStub flowIngesterStub;
    protected void initialStubs() {
        flowIngesterStub = IngesterGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public void pushFlowToIngester(final FlowDocument flow) {
        log.trace("Pushing flow to ingester.");
        flowIngesterStub.storeFlowDocument(StoreFlowDocumentRequest.newBuilder()
            .setDocument(flow).build());
        log.trace("Done pushing flow to ingester.");
    }
}
