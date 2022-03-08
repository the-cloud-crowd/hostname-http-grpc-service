package x.hostname.grpc.service;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameServiceGrpc;
import x.hostname.grpc.utils.UserIdCallCredential;

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

        for (int i = 0; i < 10; i++) {
            HostnameServiceGrpc.HostnameServiceBlockingStub blockingStub = HostnameServiceGrpc.newBlockingStub(channel)
                    .withCallCredentials(new UserIdCallCredential("john"));

            // Metadata metadata = new Metadata();
            // metadata.put(keyUserId, "john");
            // MetadataUtils.attachHeaders(blockingStub, metadata);

            HostnameReply reply = blockingStub.getHostname(Empty.getDefaultInstance());

            System.out.println(reply);
        }

        channel.shutdown();
    }

}
