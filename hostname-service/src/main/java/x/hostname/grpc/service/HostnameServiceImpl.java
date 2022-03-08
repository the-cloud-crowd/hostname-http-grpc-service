
package x.hostname.grpc.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameServiceGrpc.HostnameServiceImplBase;
import x.hostname.grpc.utils.HostnameUtils;

public final class HostnameServiceImpl extends HostnameServiceImplBase {

    private static final Logger logger = LogManager.getLogger(HostnameServiceImpl.class);

    @Override
    public void getHostname(Empty request, StreamObserver<HostnameReply> responseObserver) {

        logger.info("Process request");

        HostnameReply reply = HostnameReply.newBuilder().setHostname(HostnameUtils.determineHostname()).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

}
