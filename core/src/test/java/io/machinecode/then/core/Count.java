package io.machinecode.then.core;

import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnProgress;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
* @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
*/
class Count<T,F,P> implements OnComplete, OnResolve<T>, OnReject<F>, OnCancel, OnProgress<P>, Future<T> {
    int count = 0;

    @Override
    public void complete(final int state) {
        ++count;
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
        ++count;
        return null;
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ++count;
        return null;
    }

    @Override
    public boolean cancel(final boolean mayInterrupt) {
        ++count;
        return false;
    }

    @Override
    public void reject(final F fail) {
        ++count;
    }

    @Override
    public void resolve(final T that) {
        ++count;
    }

    @Override
    public void progress(final P that) {
        ++count;
    }
}
