package info.kgeorgiy.ja.malko.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class PoolShutdowner {
    private static int SECONDS = 2;

    public static void shutdown(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Pool didn't terminated");
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
