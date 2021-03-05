package demo.synchronizers.part2;

import demo.common.Demo;

import java.util.concurrent.Phaser;

public class Phaser_OneTimeEntryBarrier extends Demo {

    private static final int PARTIES = 3;

    public static void main(String[] args) {
        Phaser phaser = new Phaser(1);
        log("after constructor", phaser);

        for (int p = 0; p < PARTIES; p++) {
            int delay = p + 1;
            Runnable task = new Worker(delay, phaser);
            new Thread(task).start();
        }

        log("all threads waiting to start", phaser);
        sleep(1);

        log("before all threads started", phaser);
        phaser.arriveAndDeregister();
        log("after all threads started", phaser);

        sleep(10);
        log("all threads finished", phaser);
    }

    private static void log(String message, Phaser phaser) {
        logger.info("{} phase: {}, registered/arrived/unarrived: {}={}+{}, terminated: {}",
                String.format("%-40s", message),
                phaser.getPhase(),
                phaser.getRegisteredParties(),
                phaser.getArrivedParties(),
                phaser.getUnarrivedParties(),
                phaser.isTerminated());
    }

    private static class Worker implements Runnable {

        private final int delay;
        private final Phaser phaser;

        Worker(int delay, Phaser phaser) {
            phaser.register();

            this.delay = delay;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            log("before arriveAndAwaitAdvance()", phaser);
            phaser.arriveAndAwaitAdvance();
            log("after arriveAndAwaitAdvance()", phaser);
            work();
        }

        private void work() {
            logger.info("work {} started", delay);
            sleep(delay);
            logger.info("work {} finished", delay);
        }
    }
}
