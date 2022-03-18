package x.hostname.grpc.interceptors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class DefaultServerInterceptor implements ServerInterceptor {

    private static final Logger logger = LogManager.getLogger(DefaultServerInterceptor.class.getName());

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders, ServerCallHandler<ReqT, RespT> next) {
        logger.info("headers received from client: {}", requestHeaders);
        return next.startCall(call, requestHeaders);
    }
}
