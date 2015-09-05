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

import io.machinecode.then.api.Deferred;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class FutureDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements Callable<T>, Runnable {

    protected final Future<? extends T> future;
    protected final long timeout;
    protected final TimeUnit unit;

    public FutureDeferred(final Future<? extends T> future) {
        this.future = future;
        this.timeout = -1;
        this.unit = null;
    }

    public FutureDeferred(final Future<? extends T> future, final long timeout, final TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public void run() {
        getFuture(future, this, timeout, unit);
    }

    @Override
    public T call() throws Exception {
        run();
        return unit == null
                ? get()
                : get(timeout, unit);
    }

    public Runnable asRunnable() {
        return this;
    }

    public Callable<T> asCallable() {
        return this;
    }

    public static <T,P> void getFuture(final Future<? extends T> future, final Deferred<T,Throwable,P> def, final long timeout, final TimeUnit unit) {
        final T that;
        try {
            if (unit == null) {
                that = future.get();
            } else {
                that = future.get(timeout, unit);
            }
        } catch (final CancellationException e) {
            def.cancel(true);
            return;
        } catch (final Throwable e) {
            def.reject(e);
            return;
        }
        def.resolve(that);
    }
}
