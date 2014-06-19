package io.machinecode.then.api;

import java.util.concurrent.Future;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Promise<T> extends OnResolve<T>, OnReject<Throwable>, Future<T> {

    byte PENDING  = 0;
    byte RESOLVED = 1;
    byte REJECTED = 2;

    @Override
    void resolve(final T that) throws ResolvedException, RejectedException;

    @Override
    void reject(final Throwable that) throws ResolvedException, RejectedException;

    boolean isResolved();

    boolean isRejected();

    byte getState();

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
}
