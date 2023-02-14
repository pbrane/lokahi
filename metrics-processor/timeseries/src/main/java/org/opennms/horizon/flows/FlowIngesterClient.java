package org.opennms.horizon.flows;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import org.opennms.dataplatform.flows.document.FlowDocument;
import org.opennms.dataplatform.flows.ingester.v1.IngesterGrpc;
import org.opennms.dataplatform.flows.ingester.v1.StoreFlowDocumentRequest;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

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
        System.out.println("MOO: Triggering flow push");
        pushFlowToIngester();
    }

    public void pushFlowToIngester() {
        System.out.println("MOO: Starting!");
        FlowDocument flowDocument = FlowDocument.newBuilder()
            .setApplication("sonos")
            .build();
        flowIngesterStub.storeFlowDocument(StoreFlowDocumentRequest.newBuilder()
            .setDocument(flowDocument).build());
        System.out.println("MOO: DONE!");
    }
}
