package org.opennms.horizon.flows;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.dataplatform.flows.document.FlowDocument;
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

    @EventListener(ApplicationReadyEvent.class)
    public void triggerFlowPushAfterStartup() {
        log.info("Triggering flow push...");
        pushFlowToIngester();
    }

    public void pushFlowToIngester() {
        log.info("Pushing flow to ingester.");
        FlowDocument flowDocument = FlowDocument.newBuilder()
            .setApplication("sonos")
            .build();
        flowIngesterStub.storeFlowDocument(StoreFlowDocumentRequest.newBuilder()
            .setDocument(flowDocument).build());
        log.info("Done pushing flow to ingester.");
    }
}
