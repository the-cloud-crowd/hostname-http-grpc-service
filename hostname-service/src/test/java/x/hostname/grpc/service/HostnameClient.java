package x.hostname.grpc.service;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import x.hostname.grpc.interceptors.DefaultClientInterceptor;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameServiceGrpc;

public class HostnameClient {

    public static void main(String[] args) {

        if (args == null || args.length < 1) {
            System.out.println("Target parameter is missing");
            return;
        }
        String target = args[0];

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(target);
        if (target.startsWith("localhost")) {
            channelBuilder.usePlaintext();
        }

        ManagedChannel channel = channelBuilder.build();

        Channel interceptedChannel = ClientInterceptors.intercept(channel,
                new DefaultClientInterceptor("1.0.0", "john"));

        for (int i = 0; i < 10; i++) {

            HostnameServiceGrpc.HostnameServiceBlockingStub blockingStub = HostnameServiceGrpc
                    .newBlockingStub(interceptedChannel);

            // Metadata metadata = new Metadata();
            // metadata.put(keyUserId, "john");
            // MetadataUtils.attachHeaders(blockingStub, metadata);

            HostnameReply reply = blockingStub.getHostname(Empty.getDefaultInstance());

            System.out.println(reply);
        }

        channel.shutdown();
    }

}
