package demo.synchronizers.part2;

import demo.common.Demo;

import java.util.concurrent.CountDownLatch;

public class CountDownLatch_OneTimeEntryAndExitBarriers extends Demo {

    private static final int PARTIES = 3;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch entryBarrier = new CountDownLatch(1);
        CountDownLatch exitBarrier = new CountDownLatch(PARTIES);

        for (int p = 0; p < PARTIES; p++) {
            int delay = p + 1;
            Runnable task = new Worker(delay, entryBarrier, exitBarrier);
            new Thread(task).start();
        }

        logger.info("all threads waiting to start");
        sleep(1);

        entryBarrier.countDown();
        logger.info("all threads started");

        exitBarrier.await();
        logger.info("all threads finished");
    }

    private static class Worker implements Runnable {

        private final int delay;
        private final CountDownLatch entryBarrier;
        private final CountDownLatch exitBarrier;

        Worker(int delay, CountDownLatch entryBarrier, CountDownLatch exitBarrier) {
            this.delay = delay;
            this.entryBarrier = entryBarrier;
            this.exitBarrier = exitBarrier;
        }

        @Override
        public void run() {
            try {
                entryBarrier.await();
                work();
                exitBarrier.countDown();
            } catch (InterruptedException e) {
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
