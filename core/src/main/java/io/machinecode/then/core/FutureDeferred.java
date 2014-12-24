package io.machinecode.then.core;

import io.machinecode.then.api.ExecutablePromise;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class FutureDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements ExecutablePromise<T,Throwable,P>, Callable<T>, Runnable {

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
        final T that;
        try {
            if (this.unit == null) {
                that = future.get();
            } else {
                that = future.get(timeout, unit);
            }
        } catch (final CancellationException e) {
            cancel(true);
            return;
        } catch (final Throwable e) {
            reject(e);
            return;
        }
        resolve(that);
    }

    @Override
    public T call() throws Exception {
        run();
        return unit == null
                ? get()
                : get(timeout, unit);
    }

    @Override
    public Runnable asRunnable() {
        return this;
    }

    @Override
    public Callable<T> asCallable() {
        return this;
    }
}
