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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class ThrowingFuture<T> implements Future<T> {

    final boolean[] called;
    final int index;

    public ThrowingFuture(final boolean[] called, final int index) {
        this.called = called;
        this.index = index;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isCancelled() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isDone() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        called[index] = true;
        throw new RuntimeException();
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        called[index] = true;
        throw new RuntimeException();
    }
}
