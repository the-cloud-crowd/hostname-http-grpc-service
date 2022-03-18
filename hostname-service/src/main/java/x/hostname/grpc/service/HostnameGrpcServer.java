package x.hostname.grpc.service;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;

public final class HostnameGrpcServer {

    private static final Logger logger = LogManager.getLogger(HostnameGrpcServer.class);

    private int port = 50051;
    private Server server;

    public void run() {
        try {
            HealthStatusManager health = new HealthStatusManager();
            server = ServerBuilder.forPort(port)
                    .addService(new HostnameServiceImpl())
                    .addService(new CounterServiceImpl())
                    .addService(ProtoReflectionService.newInstance())
                    .addService(health.getHealthService())
                    .build()
                    .start();

            logger.info("Listening on port {}", port);

            // This would normally be tied to the service's dependencies. For example, if HostnameGreeter
            // used a Channel to contact a required service, then when 'channel.getState() ==
            // TRANSIENT_FAILURE' we'd want to set NOT_SERVING. But HostnameGreeter has no dependencies, so
            // hard-coding SERVING is appropriate.
            health.setStatus("", ServingStatus.SERVING);
            server.awaitTermination();
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Failed to start gRPC server");
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        if (server != null) {
            // Start graceful shutdown
            server.shutdown();
            try {
                // Wait for RPCs to complete processing
                if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                    // That was plenty of time. Let's cancel the remaining RPCs
                    server.shutdownNow();
                    // shutdownNow isn't instantaneous, so give a bit of time to clean resources up
                    // gracefully. Normally this will be well under a second.
                    server.awaitTermination(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                server.shutdownNow();
            }
        }
    }
}
