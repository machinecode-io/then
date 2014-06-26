package io.machinecode.then.api;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Promise<T, F extends Throwable> extends OnResolve<T>, OnReject<F>, OnCancel, Future<T> {

    byte PENDING  = 0;
    byte RESOLVED = 1;
    byte REJECTED = 2;
    byte CANCELLED = 3;

    @Override
    void resolve(final T that) throws ResolvedException, RejectedException, CancelledException;

    @Override
    void reject(final F that) throws ResolvedException, RejectedException, CancelledException;

    boolean isResolved();

    boolean isRejected();

    /**
     * Triggered on any event after which {@link #isResolved()} ()} will return true.
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F> onResolve(final OnResolve<T> then);

    /**
     * Triggered on any event after which {@link #isRejected()} ()} will return true.
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F> onReject(final OnReject<F> then);

    /**
     * Triggered on any event after which {@link #isCancelled()} will return true.
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F> onCancel(final OnCancel then);

    /**
     * Triggered on any event after which {@link #isDone()} will return true;
     * Will be fired in addition to the callback for the specific event.
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F> onComplete(final OnComplete then);

    /**
     * Triggered when {@link #get(long, TimeUnit)} or {@link #get()} is called.
     * It will be called after this promise has transitioned into a state
     * where {@link #isDone()} will return true.
     *
     * Each get method will call the corresponding get method on the {@link Future}
     * in the thread that called either {@link Promise#get()} or {@link Promise#get(long, TimeUnit)}.
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F> onGet(final Future<?> then);
}
