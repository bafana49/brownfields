package za.co.wethinkcode.robots.world;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldGateTest {

    @Test
    @DisplayName("serialized gate runs overlapping jobs one after another")
    void serialized_runsJobsInSequence() throws InterruptedException {
        WorldGate gate = WorldGate.serialized();
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger maxInside = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch done = new CountDownLatch(2);

        Runnable job = () -> gate.run(() -> {
            int now = inside.incrementAndGet();
            maxInside.accumulateAndGet(now, Math::max);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inside.decrementAndGet();
            return null;
        });

        new Thread(() -> {
            ready.countDown();
            try {
                ready.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            job.run();
            done.countDown();
        }).start();
        new Thread(() -> {
            ready.countDown();
            try {
                ready.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            job.run();
            done.countDown();
        }).start();

        done.await();
        gate.shutdown();
        assertEquals(1, maxInside.get());
    }

    @Test
    @DisplayName("immediate gate runs on the caller")
    void immediate_runsOnCallingThread() {
        WorldGate gate = WorldGate.immediate();
        List<String> names = new ArrayList<>();
        gate.run(() -> names.add(Thread.currentThread().getName()));
        assertEquals(List.of(Thread.currentThread().getName()), names);
    }
}
