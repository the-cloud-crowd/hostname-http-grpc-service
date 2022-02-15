package x.hostname.service;

import java.util.concurrent.CompletableFuture;

import x.hostname.grpc.service.HostnameGrpcServer;

public class HostnameServerGrpcOnly {

    public static void main(String[] args) {

        final HostnameGrpcServer grpcServer = new HostnameGrpcServer();
        CompletableFuture<Void> grpcFuture = CompletableFuture.runAsync(grpcServer::run);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                CompletableFuture.runAsync(grpcServer::shutdown);
            }
        });

        CompletableFuture.allOf(grpcFuture).join();
    }

}
