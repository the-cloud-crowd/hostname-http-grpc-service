package x.hostname.http.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spark.Spark;
import x.hostname.service.HostnameUtils;

public class HostnameHttpServer {

    private int port = 8080;

    private static final Logger logger = LogManager.getLogger(HostnameHttpServer.class);

    public void run() {
        try {
            Spark.port(port);
            Spark.get("hostname", (req, res) -> {
                res.type("application/json; charset=utf-8");
                return String.format("{\"hostname\":\"%s\"}%n", HostnameUtils.determineHostname());
            });
            logger.info("Listening on port {}", port);
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Failed to start HTTP server");
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        Spark.stop();
    }

}
