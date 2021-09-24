package x.hostname.service;

import java.util.concurrent.CompletableFuture;

import x.hostname.grpc.service.HostnameGrpcServer;
import x.hostname.http.service.HostnameHttpServer;

public class HostnameServer {

    public static void main(String[] args) {

        final HostnameGrpcServer grpcServer = new HostnameGrpcServer();

        final HostnameHttpServer httpServer = new HostnameHttpServer();

        CompletableFuture<Void> grpcFuture = CompletableFuture.runAsync(grpcServer::run);
        CompletableFuture<Void> httpFuture = CompletableFuture.runAsync(httpServer::run);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                CompletableFuture.runAsync(grpcServer::shutdown);
                CompletableFuture.runAsync(httpServer::shutdown);
            }
        });

        CompletableFuture.allOf(grpcFuture, httpFuture).join();
    }

}
