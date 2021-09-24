package x.hostname.service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HostnameUtils {

    private static final Logger logger = LogManager.getLogger(HostnameUtils.class);

    private HostnameUtils() {
    }

    public static String determineHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException ex) {
            logger.atWarn().withThrowable(ex).log("Failed to determine hostname. I will generate one.");
        }
        if (System.getenv().containsKey("HOSTNAME")) {
            return System.getenv().get("HOSTNAME");
        }
        // Strange. Well, let's make an identifier for ourselves.
        return "random-" + new Random().nextInt();
    }

}
