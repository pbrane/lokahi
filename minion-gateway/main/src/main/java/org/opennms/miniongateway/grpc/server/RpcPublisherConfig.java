package org.opennms.miniongateway.grpc.server;

import java.util.concurrent.atomic.AtomicBoolean;
import org.opennms.core.ipc.grpc.server.OpennmsGrpcServer;
import org.opennms.horizon.grpc.echo.contract.EchoRequest;
import org.opennms.horizon.grpc.echo.contract.EchoResponse;
import org.opennms.horizon.shared.ipc.rpc.api.server.CallFactory;
import org.opennms.horizon.shared.ipc.rpc.api.server.CallFactory.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcPublisherConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EchoRequester echoRequester(OpennmsGrpcServer server) {
        return new EchoRequester(server);
    }

}

class EchoRequester implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(EchoRequester.class);
    private final OpennmsGrpcServer server;
    private AtomicBoolean run = new AtomicBoolean(true);


    public EchoRequester(OpennmsGrpcServer server) {
        this.server = server;
    }

    private void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private void stop() {
        run.set(false);
    }

    @Override
    public void run() {
        while (run.get()) {
            CallFactory callFactory = server.getCallFactory();

            EchoRequest request = EchoRequest.newBuilder()
                .setBody("foo")
                .setId(1)
                .setDelay(1000)
                .build();
            Call<EchoResponse> call = callFactory.create(request, EchoResponse::parseFrom)
                .withLocation("cloud")
                .withSystem("")
                .withModule("echo")
                .build();

            call.execute().whenComplete((echo, err) -> {
                if (err != null) {
                    logger.error("Could not perform request", err);
                    return;
                }
                logger.info("Received echo answer {}", echo);
            });

            try {
                Thread.sleep(15_000L);
            } catch (InterruptedException e) {
                logger.debug("Echo requester was interrupted", e);
            }
        }
    }
}
