package demo.synchronizers.part2;

import demo.common.Demo;

import java.util.concurrent.Phaser;

public class Phaser_CyclicEntryAndExitBarriers extends Demo {

    private static final int PARTIES = 3;
    private static final int ITERATIONS = 3;

    public static void main(String[] args) {
        Phaser phaser = new Phaser(1) {
            final private int maxPhase = ITERATIONS;

            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                return (phase >= maxPhase - 1) || (registeredParties == 0);
            }
        };
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

        phaser.register();
        while (!phaser.isTerminated()) {
            phaser.arriveAndAwaitAdvance();
        }

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
            do {
                work();
                log("before arriveAndAwaitAdvance()", phaser);
                phaser.arriveAndAwaitAdvance();
                log("after arriveAndAwaitAdvance()", phaser);
            } while (!phaser.isTerminated());
        }

        void work() {
            logger.info("work {} started", delay);
            sleep(delay);
            logger.info("work {} finished", delay);
        }
    }
}
