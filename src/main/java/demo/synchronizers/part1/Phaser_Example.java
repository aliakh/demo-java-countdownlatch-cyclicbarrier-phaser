package demo.synchronizers.part1;

import demo.common.Demo;

import java.util.concurrent.*;

public class Phaser_Example extends Demo {

    private static final int PARTIES = 1 + 3;

    public static void main(String[] args) {
        Phaser phaser = new Phaser(1);
        log("after constructor", phaser);

        phaser.bulkRegister(PARTIES - 1);
        log("after bulkRegister(3)", phaser);

        for (int p = 0; p < PARTIES - 2; p++) {
            final int delay = p + 1;

            Thread thread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(delay);

                    log("before arrive() " + delay, phaser);
                    phaser.arrive();
                    log("after arrive() " + delay, phaser);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }

        for (int p = PARTIES - 2; p < PARTIES - 1; p++) {
            final int delay = p + 1;

            Thread thread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(delay);

                    log("before arriveAndDeregister() " + delay, phaser);
                    phaser.arriveAndDeregister();
                    log("after arriveAndDeregister() " + delay, phaser);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }

        log("before arriveAndAwaitAdvance()", phaser);
        phaser.arriveAndAwaitAdvance();
        log("after arriveAndAwaitAdvance()", phaser);
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
}

