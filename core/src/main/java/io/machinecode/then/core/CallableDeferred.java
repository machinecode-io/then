package io.machinecode.then.core;

import io.machinecode.then.api.ExecutablePromise;
import io.machinecode.then.api.Promise;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class CallableDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements ExecutablePromise<T,Throwable,P>, Promise<T,Throwable,P>, Callable<T>, Runnable {

    protected final Callable<? extends T> call;

    public CallableDeferred(final Callable<? extends T> call) {
        this.call = call;
    }

    @Override
    public void run() {
        final T that;
        try {
            that = call.call();
        } catch (final Throwable e) {
            reject(e);
            return;
        }
        resolve(that);
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
