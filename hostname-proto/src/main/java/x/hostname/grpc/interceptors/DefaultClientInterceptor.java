package x.hostname.grpc.interceptors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class DefaultClientInterceptor implements ClientInterceptor {

    private static final Logger logger = LogManager.getLogger(DefaultClientInterceptor.class.getName());

    @VisibleForTesting
    static final Metadata.Key<String> USERID_HEADER_KEY = Metadata.Key.of("x-userid", Metadata.ASCII_STRING_MARSHALLER);

    @VisibleForTesting
    static final Metadata.Key<String> ASSISTANT_VERSION_HEADER_KEY = Metadata.Key.of("x-assistantversion",
            Metadata.ASCII_STRING_MARSHALLER);

    private final String assistantVersion;
    private final String userId;

    public DefaultClientInterceptor(String assistantVersion, String userId) {
        this.assistantVersion = assistantVersion;
        this.userId = userId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                headers.put(ASSISTANT_VERSION_HEADER_KEY, assistantVersion);
                headers.put(USERID_HEADER_KEY, userId);

                logger.info("Header send to server: {}", headers);

                super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onHeaders(Metadata headers) {
                        logger.info("Header received from server: {}", headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }

}
