package demo.synchronizers.part2;

import demo.common.Demo;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrier_CyclicEntryAndExitBarriers extends Demo {

    private static final int PARTIES = 3;
    private static final int ITERATIONS = 3;

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier entryBarrier = new CyclicBarrier(PARTIES + 1, () -> logger.info("iteration started"));
        CyclicBarrier exitBarrier = new CyclicBarrier(PARTIES + 1, () -> logger.info("iteration finished"));

        for (int i = 0; i < ITERATIONS; i++) {
            for (int p = 0; p < PARTIES; p++) {
                int delay = p + 1;
                Runnable task = new Worker(delay, entryBarrier, exitBarrier);
                new Thread(task).start();
            }

            logger.info("all threads waiting to start: iteration {}", i);
            sleep(1);

            entryBarrier.await();
            logger.info("all threads started: iteration {}", i);

            exitBarrier.await();
            logger.info("all threads finished: iteration {}", i);
        }
    }

    private static class Worker implements Runnable {

        private final int delay;
        private final CyclicBarrier entryBarrier;
        private final CyclicBarrier exitBarrier;

        Worker(int delay, CyclicBarrier entryBarrier, CyclicBarrier exitBarrier) {
            this.delay = delay;
            this.entryBarrier = entryBarrier;
            this.exitBarrier = exitBarrier;
        }

        @Override
        public void run() {
            try {
                entryBarrier.await();
                work();
                exitBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        private void work() {
            logger.info("work {} started", delay);
            sleep(delay);
            logger.info("work {} finished", delay);
        }
    }
}
