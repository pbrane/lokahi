package org.opennms.horizon.email.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class GrpcServerManager {
    private Server grpcServer;
    private final int port;
    private final ServerInterceptor interceptor;

    public synchronized void startServer(BindableService... services) {
        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(port))
            .intercept(interceptor)
            .addService(ProtoReflectionService.newInstance());
        Arrays.stream(services).forEach(serverBuilder::addService);
        grpcServer = serverBuilder.build();
        try {
            grpcServer.start();
            log.info("Inventory gRPC server started at port {}", port);
        } catch (IOException e) {
            log.error("Couldn't start inventory gRPC server", e);
        }
    }

    public synchronized void stopServer() throws InterruptedException {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            grpcServer.shutdown();
            grpcServer.awaitTermination();
        }
    }
}
