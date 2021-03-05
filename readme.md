# Java concurrency and parallelism: barrier synchronizers (CountDownLatch, CyclicBarrier, Phaser)

## Introduction

Barrier synchronizers (barriers) are a kind of synchronizer that ensures that any threads must stop at a certain point and cannot proceed further until all other threads reach this point.

By purpose, barriers can be grouped into the following categories:

*   entry barriers, that prevents threads from starting processing
*   exit barriers, that waiting for all threads to finish processing

Barriers also can be grouped by the number of iterations (one-time or cyclic) and by the number of parties/threads (fixed or variable).

In Java 7+ there are 3 predefined barrier classes: _CountDownLatch_, _CyclicBarrier_, _Phaser_.

## The CountDownLatch class

The _CountDownLatch_ class is a one-time barrier that allows threads to wait until the given count of operations is performed in other threads. 

![CountDownLatch](/images/CountDownLatch.png)

A latch is initialized with a given count. The _await_ methods (waiting and timed waiting) wait until the current count reaches 0 due to calls of the _countDown()_ method. After that, all waiting threads are released, and any subsequent calls of the _await_ methods return immediately. 

![CountDownLatch example](/images/CountDownLatch_example.png)

#### Threads registration

The _CountDownLatch(int count)_ constructor creates a latch with the given count. The current count cannot be reset without recreating a new latch object. 

#### Threads waiting

The _void await()_ method causes the current thread to wait until one of the events occurs:

*   the latch has counted down to 0 due to calls of the _countDown_() method 
*   the thread is interrupted

If the current count is 0 then this method returns immediately. 

The _boolean await(long timeout, TimeUnit unit)_ method causes the current thread to wait until one of the events occurs:

*   the given timeout elapses
*   the latch has counted down to 0 due to calls of the _countDown_() method 
*   the thread is interrupted

The method returns _true_ if the current count reached 0 and _false_ if the timeout elapsed before the current count reached 0. If the current count is 0 then this method returns _true_ immediately.

#### Threads arrival

The _countDown()_ method decrements the current count, releasing all waiting threads if the count reaches 0. If the current count equals 0 then nothing happens. 

#### Latch monitoring

The _long getCount()_ method returns the current count of the latch.

#### Example

In the example are used 2 latches: first as a one-time entry barrier, second as a one-time exit barrier.

```
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
```

## The CyclicBarrier class

The _CyclicBarrier_ class is a reusable synchronization barrier that allows threads to wait for each other at a certain point. 

![CyclicBarrier](/images/CyclicBarrier.png)

A barrier is initialized with a given number of threads. The _await_ methods (waiting and timed waiting) wait until all threads reach the barrier. Then all threads trip the barrier, and the barrier is automatically reset for the next cycle.

![CyclicBarrier example](/images/CyclicBarrier_example.png)

#### Threads registration

The _CyclicBarrier(int parties)_ constructor creates a new barrier that will trip when the given number of threads are waiting upon it.

The _CyclicBarrier(int parties, Runnable barrierAction)_ constructor creates a new barrier that will trip when the given number of threads are waiting upon it. When the barrier is tripped, the given barrier action will be performed by the last thread entering the barrier. 

#### Threads arrival and waiting

The _int await()_ method causes the current thread to wait until one of the events occurs:

*   the last thread arrives at the barrier
*   the barrier is broken (by the reasons described below)

If the barrier is broken, then depending on the reason _InterruptedException_, _BrokenBarrierException_ are thrown.

The _int await(long timeout, TimeUnit unit)_ method causes the current thread to wait until one of the events occurs:

*   the given timeout elapses
*   the last thread arrives at the barrier
*   the barrier is broken (by the reasons described below)

If the specified timeout elapses, then a _TimeoutException_ is thrown. If the barrier is broken, then depending on the reason _InterruptedException_, _BrokenBarrierException_ are thrown.

The _await_ method returns the arrival index of the current thread.

#### Barrier reset

The _void reset()_ method resets the barrier to its initial state. If any threads are waiting at the barrier on the _await_ methods, the methods will throw a _BrokenBarrierException_.

#### Barrier monitoring

The _int getParties()_ method returns the number of parties required to trip the barrier.

The _int getNumberWaiting()_ method returns the number of parties currently waiting at the barrier.

The _boolean isBroken()_ method returns _true_ if this barrier has been broken by one of the reasons: 

*   interruption
*   timeout elapsing
*   calling the _reset()_ method
*   the barrier action failure due to an exception

#### Example

In the example are used 2 barriers: first as a cyclic entry barrier, second as a cyclic exit barrier.

```
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
```

## The Phaser class

The _Phaser_ class is a reusable barrier that allows a variable number of parties/threads. Because of this, it’s more flexible, however much more complicated.

![Phaser](/images/Phaser.png)

To support a variable number of parties, a phaser contains the number of registered, arrived, and unarrived parties. The number of registered parties always equals the sum of the numbers of arrived and unarrived parties (registered==arrived+unarrived). To support cyclic iterations, a phaser contains a number of the current phase.

![Phaser example](/images/Phaser_example.png)

#### Parties registration

The _Phaser()_ constructor creates a phaser with initial phase number 0 and no registered parties (phase=0, registered=0). The _Phaser(int parties)_ constructor creates a phaser with initial phase number 0 and the given number of registered parties (phase=0, registered=parties).

The _int register()_ method adds an unarrived party to the phaser (registered++). The _int bulkRegister(int parties)_ method adds the given number of unarrived parties to the phaser (registered+=parties). These methods return the arrival phase number to which this registration is applied.

#### Parties synchronization

The _int arrive()_ method marks a party arriving at the phaser, without waiting for other parties to arrive (arrived++, unarrived--).

The _int awaitAdvance(int phase)_ method awaits the phase of the phaser to advance from the given phase number. The method returns immediately if the current phase number is not equal to the given phase number.

The _int arriveAndAwaitAdvance()_ marks a party arriving at the phaser and awaits other parties to arrive  (arrived++, unarrived--).

The _int arriveAndDeregister()_ method marks a party arriving at the phaser and deregisters from it without waiting for other parties to arrive (registered--, arrived++, unarrived--). 

The _arrive_, _arriveAndAwaitAdvance_, _arriveAndDeregister_ methods return the arrival phase number. The _awaitAdvance_ method returns the next arrival phase number.

#### Phases iterations

The current phase is finished when all registered parties arrive (registered==arrived, unarrived==0). To decide whether to start the next phase or to terminate the phaser is used the _protected boolean onAdvance(int phase, int registeredParties)_ method. 

If the _onAdvance_ method returns _true_, then the phaser is terminated (phase&lt;0, terminated=true). If the _onAdvance_ method returns _false_, then the phaser starts a new phase (phase++, arrived=0, unarrived=registered). The _onAdvance_ method can also be used to perform a barrier action.

By default the _onAdvance_ method returns _true_ when the number of registered parties has become 0 as the result of calls the _arriveAndDeregister_ method:

```
protected boolean onAdvance(int phase, int registeredParties) {
   return registeredParties == 0;
}
```

The overridden _onAdvance_ method for one-time process:

```
@Override
protected boolean onAdvance(int phase, int registeredParties) {
   return true;
}
```

The overridden _onAdvance_ method for infinite iterations:

```
@Override
protected boolean onAdvance(int phase, int registeredParties) {
   return false;
}
```

The overridden _onAdvance_ method for _maxPhase_ iterations:

```
@Override
protected boolean onAdvance(int phase, int registeredParties) {
   return (phase >= maxPhase - 1) || (registeredParties == 0);
}
```

#### Phaser termination

Phaser is terminated automatically when the _onAdvance_ method returns _true_. It’s possible to terminate the phaser manually by calling the _forceTermination()_ method.

The _arrive_, _awaitAdvance_, _arriveAndAwaitAdvance, arriveAndDeregister_ methods return negative values if the phaser has already terminated.

#### Phaser monitoring

The methods to monitor parties numbers:

*   _int getRegisteredParties()_ - returns the number of parties registered at the phaser
*   _int getArrivedParties()_ - returns the number of registered parties that have arrived at the current phase of the phaser
*   _int getUnarrivedParties()_ - returns the number of registered parties that have not yet arrived at the current phase of the phaser

The _int getPhase()_ method returns the current phase number.

The _boolean isTerminated()_ method returns _true_ if this phaser has been terminated.

#### Examples

In the example are used the basic phaser methods.

```
public static void main(String[] args) {
   Phaser phaser = new Phaser(3) {
       @Override
       protected boolean onAdvance(int phase, int registeredParties) {
           log("inside onAdvance()", this);
           return true;
       }
   };
   log("after constructor", phaser);

   phaser.register();
   log("after register()", phaser);

   phaser.arrive();
   log("after arrive()", phaser);

   Thread thread = new Thread() {
       @Override
       public void run() {
           log("before arriveAndAwaitAdvance()", phaser);
           phaser.arriveAndAwaitAdvance();
           log("after arriveAndAwaitAdvance()", phaser);
       }
   };
   thread.start();

   phaser.arrive();
   log("after arrive()", phaser);

   phaser.arriveAndDeregister();
   log("after arriveAndDeregister()", phaser);
}
```

In the example, a phaser is used to implement a one-time entry barrier.

```
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
       phaser.arriveAndAwaitAdvance();
       work();
   }

   private void work() {
       logger.info("work {} started", delay);
       sleep(delay);
       logger.info("work {} finished", delay);
   }
}
```

In the example, a phaser is used to implement one-time entry and exit barriers.

```
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

   phaser.register();
   while (!phaser.isTerminated()) {
       phaser.arriveAndAwaitAdvance();
       phaser.arriveAndDeregister();
   }

   log("all threads finished", phaser);
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
       phaser.arriveAndAwaitAdvance();
       work();
       phaser.arriveAndDeregister();
   }

   private void work() {
       logger.info("work {} started", delay);
       sleep(delay);
       logger.info("work {} finished", delay);
   }
}
```

In the example, a phaser is used to implement cyclic entry and exit barriers.

```
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
           phaser.arriveAndAwaitAdvance();
       } while (!phaser.isTerminated());
   }

   void work() {
       logger.info("work {} started", delay);
       sleep(delay);
       logger.info("work {} finished", delay);
   }
}
```

## Conclusion

The _CountDownLatch_ class is suitable for one-time iteration with a fixed number of parties. 

The _CyclicBarrier_ class is suitable for one-time and cyclic iterations with a fixed number of parties.

The _Phaser_ class is suitable for one-time and cyclic iterations with a variable number of parties. It also can be used with a fixed number of parties, however, it is an excess.

Code examples are available in the [GitHub repository](https://github.com/aliakh/demo-java-countdownlatch-cyclicbarrier-phaser).
