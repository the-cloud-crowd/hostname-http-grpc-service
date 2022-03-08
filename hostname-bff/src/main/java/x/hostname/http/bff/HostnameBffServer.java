package x.hostname.http.bff;

import static x.hostname.grpc.utils.HostnameUtils.determineHostname;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import spark.Spark;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameServiceGrpc;
import x.hostname.grpc.utils.UserIdCallCredential;

public class HostnameBffServer {

    private int port = 9090;

    private static final Logger logger = LogManager.getLogger(HostnameBffServer.class);

    private final String target;
    private ManagedChannel channel;

    public HostnameBffServer(String target) {

        this.target = target;
    }

    public void run() {
        try {

            ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(target).usePlaintext();

            channel = channelBuilder.build();

            Spark.port(port);
            Spark.get("bff/hostname", (req, res) -> {

                logger.info("Process request");

                res.type("application/json; charset=utf-8");

                HostnameServiceGrpc.HostnameServiceBlockingStub blockingStub = HostnameServiceGrpc
                        .newBlockingStub(channel);

                Optional<String> userId = Optional.ofNullable(req.headers("X-UserId"));
                if (userId.isPresent()) {
                    logger.info("Add user id header: {}", userId.get());
                    blockingStub = blockingStub.withCallCredentials(new UserIdCallCredential(userId.get()));
                } else {
                    logger.warn("User id header missing");
                }

                try {
                    HostnameReply reply = blockingStub.getHostname(Empty.getDefaultInstance());
                    res.status(200);
                    return String.format("{\"hostname\":\"%s\",\"bff\":\"%s\"}%n", reply.getHostname(),
                            determineHostname());
                } catch (StatusRuntimeException e) {
                    logger.atError().withThrowable(e).log("Backend request failed");
                    res.status(500);
                    return String.format("{\"errorMessage\":\"%s\",\"{\"errorStatus\":\"%s\"}%n", e.getMessage(),
                            e.getStatus());

                }
            });

            Spark.awaitInitialization();
            logger.info("Listening on port {}", port);
            Spark.awaitStop();
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Failed to start HTTP server");
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        Spark.stop();
    }

}
