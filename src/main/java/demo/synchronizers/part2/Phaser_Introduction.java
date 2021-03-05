package demo.synchronizers.part2;

import demo.common.Demo;

import java.util.concurrent.Phaser;

public class Phaser_Introduction extends Demo {

    public static void main(String[] args) {
        Phaser phaser = new Phaser(3) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                log("inside onAdvance", this);
                return true;
            }
        };
        log("after constructor", phaser);

        phaser.register();
        log("after register()", phaser);

        phaser.arrive();
        log("after arrive()", phaser);

        Thread thread = new Thread(() -> {
            log("before arriveAndAwaitAdvance()", phaser);
            phaser.arriveAndAwaitAdvance();
            log("after arriveAndAwaitAdvance()", phaser);
        });
        thread.start();

        phaser.arrive();
        log("after arrive()", phaser);

        phaser.arriveAndDeregister();
        log("after arriveAndDeregister()", phaser);
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
