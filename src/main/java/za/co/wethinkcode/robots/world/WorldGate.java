package za.co.wethinkcode.robots.world;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Serialises work against the live {@link World}.
 * Socket clients, the Web API, and the server console all submit jobs so only
 * one command mutates or snapshots the world at a time.
 */
public final class WorldGate {

    private final ExecutorService worker;

    private WorldGate(ExecutorService worker) {
        this.worker = worker;
    }

    /**
     * Runs work on the calling thread. Used by unit tests that do not share a world
     * across adapters.
     */
    public static WorldGate immediate() {
        return new WorldGate(null);
    }

    /**
     * One worker thread; callers wait for the result.
     */
    public static WorldGate serialized() {
        return new WorldGate(Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "world-commands");
            thread.setDaemon(true);
            return thread;
        }));
    }

    public <T> T run(Supplier<T> work) {
        if (worker == null) {
            return work.get();
        }
        try {
            return worker.submit(work::get).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        }
    }

    public void run(Runnable work) {
        run(() -> {
            work.run();
            return null;
        });
    }

    public void shutdown() {
        if (worker == null) {
            return;
        }
        worker.shutdown();
        try {
            worker.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            worker.shutdownNow();
        }
    }
}
