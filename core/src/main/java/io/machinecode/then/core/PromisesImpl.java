package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.ExecutablePromise;
import io.machinecode.then.api.Promises;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class PromisesImpl implements Promises {

    public PromisesImpl() {}

    @Override
    public <T, F, P> Deferred<T, F, P> deferred() {
        return new DeferredImpl<>();
    }

    @Override
    public <T, P> ExecutablePromise<T, Throwable, P> executable(final Callable<T> call) {
        return new CallableDeferred<>(call);
    }

    @Override
    public <T, P> ExecutablePromise<T, Throwable, P> executable(final Runnable call, final T value) {
        return new RunnableDeferred<>(call, value);
    }

    @Override
    public <T, P> ExecutablePromise<T, Throwable, P> executable(final Future<T> future) {
        return new FutureDeferred<>(future);
    }

    @Override
    public <T, P> ExecutablePromise<T, Throwable, P> executable(final Future<T> future, final long timeout, final TimeUnit unit) {
        return new FutureDeferred<>(future, timeout, unit);
    }

    @Override
    public <T, F, P> Promise<T, F, P> all(final Collection<? extends Promise<?, ?, ?>> promises) {
        return new AllDeferred<>(promises);
    }

    @Override
    public <T, F, P> Promise<T, F, P> all(final Promise<?, ?, ?>... promises) {
        return new AllDeferred<>(promises);
    }

    @Override
    public <T, F, P> Promise<T, F, P> any(final Collection<? extends Promise<?, ?, ?>> promises) {
        return new AnyDeferred<>(promises);
    }

    @Override
    public <T, F, P> Promise<T, F, P> any(final Promise<?, ?, ?>... promises) {
        return new AnyDeferred<>(promises);
    }

    @Override
    public <T, F, P> Promise<T, F, P> resolved(final T that) {
        return new ResolvedDeferred<>(that);
    }

    @Override
    public <T, F, P> Promise<T, F, P> rejected(final F fail) {
        return new RejectedDeferred<>(fail);
    }

    @Override
    public <T, F, P> Promise<T, F, P> cancelled(final boolean interrupt) {
        return new CancelledDeferred<>(interrupt);
    }
}
