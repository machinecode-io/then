/*
 * Copyright 2015 Brent Douglas and other contributors
 * as indicated by the @authors tag. All rights reserved.
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class CallableDeferredTest extends UnitTest {

    static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterClass
    public static void after() {
        executor.shutdown();
    }

    @Test
    public void testCallable() throws Exception {
        final boolean[] called = { false };
        final CallableDeferred<Void, Void> d = new CallableDeferred<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                called[0] = true;
                return null;
            }
        });
        final Future<Void> x = executor.submit(d.asCallable());
        x.get(100, TimeUnit.MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isResolved());
    }

    @Test
    public void testRunnable() throws Exception {
        final boolean[] called = { false };
        final CallableDeferred<Void, Void> d = new CallableDeferred<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                called[0] = true;
                return null;
            }
        });
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, TimeUnit.MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isResolved());
    }

    @Test
    public void testCallableRejected() throws Exception {
        final boolean[] called = { false };
        final CallableDeferred<Void, Void> d = new CallableDeferred<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                called[0] = true;
                throw new RuntimeException();
            }
        });
        final Future<Void> x = executor.submit(d.asCallable());
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
        final CallableDeferred<Void, Void> d = new CallableDeferred<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                called[0] = true;
                throw new RuntimeException();
            }
        });
        final Future<?> x = executor.submit(d.asRunnable());
        x.get(100, TimeUnit.MILLISECONDS);
        assertArrayIs(true, called);
        assertTrue(d.isRejected());
    }
}
