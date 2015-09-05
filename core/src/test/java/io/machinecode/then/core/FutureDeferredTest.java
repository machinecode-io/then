/*
 * Copyright 2015 Brent Douglas and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.machinecode.then.core;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class FutureDeferredTest extends UnitTest {

    static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterClass
    public static void after() {
        executor.shutdown();
    }

    @Test
    public void testCallable() throws Exception {
        final Count<Void,Void,Void> c = new Count<>();
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c);
        assertEquals(0, c.count);
        final Future<?> x = executor.submit(d.asCallable());
        x.get(100, MILLISECONDS);
        assertEquals(1, c.count);
        assertTrue(d.isResolved());
    }

    @Test
    public void testRunnable() throws Exception {
        final Count<Void,Void,Void> c = new Count<>();
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c);
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, MILLISECONDS);
        assertEquals(1, c.count);
        assertTrue(d.isResolved());
    }

    @Test
    public void testTimedCallable() throws Exception {
        final Count<Void,Void,Void> c = new Count<>();
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c, 10, MILLISECONDS);
        assertEquals(0, c.count);
        final Future<?> x = executor.submit(d.asCallable());
        x.get(100, MILLISECONDS);
        assertEquals(1, c.count);
        assertTrue(d.isResolved());
    }

    @Test
    public void testTimedRunnable() throws Exception {
        final Count<Void,Void,Void> c = new Count<>();
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c, 10, MILLISECONDS);
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, MILLISECONDS);
        assertEquals(1, c.count);
        assertTrue(d.isResolved());
    }

    @Test
    public void testCallableTimeout() throws Exception {
        final Count<Void,Void,Void> c = new Count<Void,Void,Void>() {
            @Override
            public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new TimeoutException();
            }
        };
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c, 5, MILLISECONDS);
        final Future<?> x = executor.submit(d.asCallable());
        try {
            x.get(100, MILLISECONDS);
            fail();
        } catch (final ExecutionException e) {}
        assertTrue(d.isRejected());
    }

    @Test
    public void testRunnableTimeout() throws Exception {
        final Count<Void,Void,Void> c = new Count<Void,Void,Void>() {
            @Override
            public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new TimeoutException();
            }
        };
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c, 5, MILLISECONDS);
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, MILLISECONDS);
        assertTrue(d.isRejected());
    }

    @Test
    public void testCallableCancelled() throws Exception {
        final Count<Void,Void,Void> c = new Count<Void,Void,Void>() {
            @Override
            public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new CancellationException();
            }
        };
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c, 5, MILLISECONDS);
        final Future<?> x = executor.submit(d.asCallable());
        try {
            x.get(100, MILLISECONDS);
            fail();
        } catch (final ExecutionException e) {}
        assertTrue(d.isCancelled());
    }

    @Test
    public void testRunnableCancelled() throws Exception {
        final Count<Void,Void,Void> c = new Count<Void,Void,Void>() {
            @Override
            public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new CancellationException();
            }
        };
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(c, 5, MILLISECONDS);
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, MILLISECONDS);
        assertTrue(d.isCancelled());
    }

    @Test
    public void testCallableRejected() throws Exception {
        final boolean[] called = { false };
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(new ThrowingFuture<Void>(called, 0));
        final Future<?> x = executor.submit(d.asCallable());
        try {
            x.get(100, MILLISECONDS);
            fail();
        } catch (final ExecutionException e) {}
        assertArrayIs(true, called);
        assertTrue(d.isRejected());
    }

    @Test
    public void testRunnableRejected() throws Exception {
        final boolean[] called = { false };
        final FutureDeferred<Void, Void> d = new FutureDeferred<>(new ThrowingFuture<Void>(called, 0));
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isRejected());
    }
}
