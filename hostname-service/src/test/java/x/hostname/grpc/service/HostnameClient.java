package x.hostname.grpc.service;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameRequest;
import x.hostname.grpc.proto.HostnameServiceGrpc;

public class HostnameClient {

    public static void main(String[] args) {

        if (args == null || args.length < 1) {
            System.out.println("Target parameter is missing");
        }
        String target = args[0];

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(target);
        if (target.startsWith("localhost")) {
            channelBuilder.usePlaintext();
        }
        ManagedChannel channel = channelBuilder.build();

        for (int i = 0; i < 10; i++) {
            HostnameServiceGrpc.HostnameServiceBlockingStub blockingStub = HostnameServiceGrpc.newBlockingStub(channel)
                    .withCallCredentials(new UserIdCallCredential("john"));

            // Metadata metadata = new Metadata();
            // metadata.put(keyUserId, "john");
            // MetadataUtils.attachHeaders(blockingStub, metadata);

            HostnameReply reply = blockingStub.getHostname(HostnameRequest.newBuilder().setSender("john").build());

            System.out.println(reply);
        }

        channel.shutdown();
    }

    public static class UserIdCallCredential extends CallCredentials {

        private final String userId;

        public UserIdCallCredential(String userId) {
            this.userId = userId;
        }

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        Metadata headers = new Metadata();
                        Metadata.Key<String> userIdKey = Metadata.Key.of("X-UserId", Metadata.ASCII_STRING_MARSHALLER);
                        headers.put(userIdKey, userId);
                        metadataApplier.apply(headers);
                    } catch (Throwable e) {
                        metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                    }
                }
            });
        }

        @Override
        public void thisUsesUnstableApi() {
        }
    }
}
