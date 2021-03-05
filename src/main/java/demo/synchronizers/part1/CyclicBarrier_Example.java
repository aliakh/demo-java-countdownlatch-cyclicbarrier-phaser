package demo.synchronizers.part1;

import demo.common.Demo;

import java.util.concurrent.*;

public class CyclicBarrier_Example extends Demo {

    private static final int PARTIES = 1 + 3;

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        CyclicBarrier barrier = new CyclicBarrier(PARTIES);
        log("after constructor", barrier);

        for (int p = 0; p < PARTIES - 1; p++) {
            final int delay = p + 1;

            Thread thread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(delay);

                    log("before await() " + delay, barrier);
                    barrier.await();
                    log("after await() " + delay, barrier);
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }

        log("before await()", barrier);
        barrier.await();
        log("after await()", barrier);
    }

    private static void log(String message, CyclicBarrier barrier) {
        logger.info("{} waiting: {}",
                String.format("%-40s", message),
                barrier.getNumberWaiting()
        );
    }
}

