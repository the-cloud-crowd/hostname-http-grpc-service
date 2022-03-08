package x.hostname.grpc.utils;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

public class UserIdCallCredential extends CallCredentials {

    private final String userId;

    public UserIdCallCredential(String userId) {
        this.userId = userId;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                Metadata.Key<String> userIdKey = Metadata.Key.of("X-UserId", Metadata.ASCII_STRING_MARSHALLER);
                headers.put(userIdKey, userId);
                metadataApplier.apply(headers);
            } catch (Throwable e) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        // noop
    }
}
