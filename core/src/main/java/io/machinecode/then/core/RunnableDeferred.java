package io.machinecode.then.core;

import io.machinecode.then.api.ExecutablePromise;
import io.machinecode.then.api.Promise;

import java.util.concurrent.Callable;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class RunnableDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements ExecutablePromise<T,Throwable,P>, Promise<T,Throwable,P>, Callable<T>, Runnable {

    protected final Runnable call;
    protected final T value;

    public RunnableDeferred(final Runnable call, final T value) {
        this.call = call;
        this.value = value;
    }

    @Override
    public void run() {
        try {
            call.run();
        } catch (final Throwable e) {
            reject(e);
            return;
        }
        resolve(value);
    }

    @Override
    public T call() throws Exception {
        run();
        return get();
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
