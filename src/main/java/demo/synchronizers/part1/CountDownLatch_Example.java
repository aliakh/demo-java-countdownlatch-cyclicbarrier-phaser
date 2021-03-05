package demo.synchronizers.part1;

import demo.common.Demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatch_Example extends Demo {

    private static final int PARTIES = 3;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        log("after constructor", latch);

        for (int p = 0; p < PARTIES; p++) {
            final int delay = p + 1;

            Thread thread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(delay);

                    log("before countDown() " + delay, latch);
                    latch.countDown();
                    log("after countDown() " + delay, latch);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }

        log("before await()", latch);
        latch.await();
        log("after await()", latch);
    }

    private static void log(String message, CountDownLatch latch) {
        logger.info("{} count: {}",
                String.format("%-40s", message),
                latch.getCount()
        );
    }
}

