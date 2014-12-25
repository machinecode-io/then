package io.machinecode.then.core;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class RunnableDeferredTest extends UnitTest {

    static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterClass
    public static void after() {
        executor.shutdown();
    }

    @Test
    public void testCallable() throws Exception {
        final boolean[] called = { false };
        final RunnableDeferred<Void, Void> d = new RunnableDeferred<>(new Runnable() {
            @Override
            public void run() {
                called[0] = true;
            }
        }, null);
        final Future<?> x = executor.submit(d.asCallable());
        x.get(100, TimeUnit.MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isResolved());
    }

    @Test
    public void testRunnable() throws Exception {
        final boolean[] called = { false };
        final RunnableDeferred<Void, Void> d = new RunnableDeferred<>(new Runnable() {
            @Override
            public void run() {
                called[0] = true;
            }
        }, null);
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, TimeUnit.MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isResolved());
    }

    @Test
    public void testCallableRejected() throws Exception {
        final boolean[] called = { false };
        final RunnableDeferred<Void, Void> d = new RunnableDeferred<>(new Runnable() {
            @Override
            public void run() {
                called[0] = true;
                throw new RuntimeException();
            }
        }, null);
        final Future<?> x = executor.submit(d.asCallable());
        try {
            x.get(100, TimeUnit.MILLISECONDS);
            fail();
        } catch (final ExecutionException e) {}
        assertArrayIs(true, called);
        assertTrue(d.isRejected());
    }

    @Test
    public void testRunnableRejected() throws Exception {
        final boolean[] called = { false };
        final RunnableDeferred<Void, Void> d = new RunnableDeferred<>(new Runnable() {
            @Override
            public void run() {
                called[0] = true;
                throw new RuntimeException();
            }
        }, null);
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, TimeUnit.MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isRejected());
    }
}
