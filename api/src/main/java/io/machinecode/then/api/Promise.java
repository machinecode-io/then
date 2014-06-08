package io.machinecode.then.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Promise<T> extends OnResolve<T>, OnReject<Throwable> {

    int PENDING = 0;
    int RESOLVED = 1;
    int REJECTED = 2;

    @Override
    void resolve(final T that) throws ResolvedException, RejectedException;

    @Override
    void reject(final Throwable that) throws ResolvedException, RejectedException;

    boolean isDone();

    boolean isResolved();

    boolean isRejected();

    int getState();

    /**
     * Triggered on any event after which {@link #getState()} will return {@link #RESOLVED};
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T> onResolve(final OnResolve<T> then);

    /**
     * Triggered on any event after which {@link #getState()} will return {@link #REJECTED};
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T> onReject(final OnReject<Throwable> then);

    /**
     * Triggered on any event after which {@link #isDone()} will return true;
     * Will be fired in addition to the callback for the specific event.
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T> onComplete(final OnComplete then);

    T get() throws InterruptedException, ExecutionException;

    T get(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException;
}
