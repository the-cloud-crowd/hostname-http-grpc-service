package x.hostname.http.bff;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.ServiceUnavailableResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import x.hostname.grpc.proto.ConnectRequest;
import x.hostname.grpc.proto.CounterServiceGrpc;
import x.hostname.grpc.proto.CounterUpdate;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameServiceGrpc;
import x.hostname.grpc.utils.HostnameUtils;
import x.hostname.grpc.utils.UserIdCallCredential;

public class HostnameBffServer {

    private int port = 9090;

    private static final Logger logger = LogManager.getLogger(HostnameBffServer.class);

    private final CountDownLatch awaitStop;

    private final String target;
    private ManagedChannel channel;

    private final String bff;

    private Map<WsContext, String> sessions = new ConcurrentHashMap<>();

    public HostnameBffServer(String target) {
        this.target = target;
        this.awaitStop = new CountDownLatch(1);
        this.bff = HostnameUtils.determineHostname();
    }

    public void run() {
        try (Javalin app = Javalin.create(this::configJavalin).start(port)) {

            ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(target).usePlaintext();

            channel = channelBuilder.build();

            app.get("/hostname", ctx -> {

                logger.info("Process request");

                HostnameServiceGrpc.HostnameServiceBlockingStub blockingStub = HostnameServiceGrpc
                        .newBlockingStub(channel);

                Optional<String> userId = Optional.ofNullable(ctx.header("X-UserId"));
                if (userId.isPresent()) {
                    logger.info("Add user id header: {}", userId.get());
                    blockingStub = blockingStub.withCallCredentials(new UserIdCallCredential(userId.get()));
                } else {
                    logger.warn("User id header missing");
                }

                try {
                    HostnameReply reply = blockingStub.getHostname(Empty.getDefaultInstance());
                    ctx.json(new BffResponsePojo(reply.getHostname(), bff));
                } catch (StatusRuntimeException e) {
                    logger.atError().withThrowable(e).log("GetHostname request failed");
                    switch (e.getStatus().getCode()) {
                    case ALREADY_EXISTS:
                        throw new ConflictResponse(e.getMessage());
                    case OUT_OF_RANGE:
                    case INVALID_ARGUMENT:
                        throw new BadRequestResponse(e.getMessage());
                    case NOT_FOUND:
                        throw new NotFoundResponse(e.getMessage());
                    case PERMISSION_DENIED:
                        throw new ForbiddenResponse(e.getMessage());
                    case UNAUTHENTICATED:
                        throw new UnauthorizedResponse(e.getMessage());
                    case UNAVAILABLE:
                    case UNIMPLEMENTED:
                        throw new ServiceUnavailableResponse(e.getMessage());
                    default:
                        throw new InternalServerErrorResponse(e.getMessage());
                    }
                }

            });

            app.ws("/counter", ws -> {
                ws.onConnect(ctx -> {

                    String sessionId = UUID.randomUUID().toString();
                    sessions.put(ctx, sessionId);
                    logger.info("WS onConnect {}", sessionId);

                    try {
                        CounterServiceGrpc.CounterServiceStub stub = CounterServiceGrpc.newStub(channel);

                        Optional<String> userId = Optional.ofNullable(ctx.header("X-UserId"));
                        if (userId.isPresent()) {
                            logger.info("Add user id header: {}", userId.get());
                            stub = stub.withCallCredentials(new UserIdCallCredential(userId.get()));
                        } else {
                            logger.warn("User id header missing");
                        }

                        stub.connect(ConnectRequest.newBuilder().setSessionId(sessionId).build(),
                                new StreamObserver<CounterUpdate>() {

                                    @Override
                                    public void onNext(CounterUpdate update) {
                                        broadcast(String.valueOf(update.getCount()));
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        broadcast("E");
                                    }

                                    @Override
                                    public void onCompleted() {
                                        broadcast("X");
                                    }
                                });
                    } catch (StatusRuntimeException e) {
                        logger.atError().withThrowable(e).log("Connect request failed");
                    }

                });
                ws.onClose(ctx -> {

                    String sessionId = sessions.remove(ctx);
                    logger.info("WS onClose {}", sessionId);

                    if (sessionId != null) {
                        try {
                            CounterServiceGrpc.CounterServiceBlockingStub blockingStub = CounterServiceGrpc
                                    .newBlockingStub(channel);

                            Optional<String> userId = Optional.ofNullable(ctx.header("X-UserId"));
                            if (userId.isPresent()) {
                                logger.info("Add user id header: {}", userId.get());
                                blockingStub = blockingStub.withCallCredentials(new UserIdCallCredential(userId.get()));
                            } else {
                                logger.warn("User id header missing");
                            }

                            blockingStub.disconnect(ConnectRequest.newBuilder().setSessionId(sessionId).build());
                        } catch (StatusRuntimeException e) {
                            logger.atError().withThrowable(e).log("Disconnect request failed");
                        }
                    }
                });
                ws.onMessage(ctx -> {
                    logger.info("WS onMessage");
                });
            });

            logger.info("Listening on port {}", port);
            awaitStop.await();
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Failed to start HTTP server");
            throw new RuntimeException(e);
        }
    }

    private void broadcast(String value) {
        sessions.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(new JSONObject().put("count", value).toString());
        });
    }

    private void configJavalin(JavalinConfig config) {
        config.addStaticFiles("/public", Location.CLASSPATH);
        config.contextPath = "/bff";
    }

    public void shutdown() {
        awaitStop.countDown();
    }

}
