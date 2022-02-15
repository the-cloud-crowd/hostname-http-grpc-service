package x.hostname.grpc.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.protobuf.Empty;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import x.hostname.grpc.proto.HostnameReply;
import x.hostname.grpc.proto.HostnameServiceGrpc;

/**
 * Unit tests for {@link HostnameServiceImpl}.
 */
@RunWith(JUnit4.class)
public class HostnameTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private HostnameServiceGrpc.HostnameServiceBlockingStub blockingStub = HostnameServiceGrpc.newBlockingStub(
            grpcCleanup.register(InProcessChannelBuilder.forName("hostname").directExecutor().build()));

    @Test
    public void sayHello_fixedHostname() throws Exception {
        grpcCleanup.register(InProcessServerBuilder.forName("hostname")
                .directExecutor()
                .addService(new HostnameServiceImpl())
                .build()
                .start());

        HostnameReply reply = blockingStub.getHostname(Empty.getDefaultInstance());
        assertNotNull(reply.getHostname());
    }

}
