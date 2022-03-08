package x.hostname.http.bff;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HostnameBff {

    private static final Logger logger = LogManager.getLogger(HostnameBff.class);

    public static void main(String[] args) {

        if (args == null || args.length < 1) {
            logger.error("Target parameter is missing");
            System.exit(1);
        }

        final HostnameBffServer httpServer = new HostnameBffServer(args[0]);

        CompletableFuture<Void> httpFuture = CompletableFuture.runAsync(httpServer::run);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                CompletableFuture.runAsync(httpServer::shutdown);
            }
        });

        CompletableFuture.allOf(httpFuture).join();
    }

}
