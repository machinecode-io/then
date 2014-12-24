package io.machinecode.then.core;

import io.machinecode.then.api.ExecutablePromise;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class RunnableDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements ExecutablePromise<T,Throwable,P>, Callable<T>, Runnable {

    protected final Runnable call;

    public RunnableDeferred(final Runnable call, final T value) {
        this.call = call;
        //This is safe, as this.value should never be read unless this.state == RESOLVED which requires calling #resolve
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
