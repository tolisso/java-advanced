package info.kgeorgiy.ja.malko.hello;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class PoolShutdowner {
    private static final int SECONDS = 4;

    public static void shutdown(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Pool didn't terminated");
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            System.err.println("Pool didn't terminated cause of interrupt");
            Thread.currentThread().interrupt();
        }
    }
}
