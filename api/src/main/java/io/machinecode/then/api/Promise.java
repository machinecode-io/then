package io.machinecode.then.api;

import java.util.concurrent.Future;

/**
 * <p>A read-only representation of a {@link Deferred} allowing listeners to be notified of state changes, though
 * clients are allowed to attempt to stop the computation using {@link #cancel(boolean)}.</p>
 *
 * @see Deferred
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface Promise<T, F extends Throwable, P> extends OnCancel, Future<T> {

    /**
     * <p>Called to attempt to stop the computation. Calling this method does not guarantee that the computation will
     * cease however if this method is the first terminal method called, an implementation MUST guarantee that any
     * listeners will not by notified of calls to any other terminal methods. After this method has
     * been called {@link #isDone()} will return {@code true}. If this was the first terminal method to be called
     * {@link #isCancelled()} will also return {@code true}.</p>
     *
     * @throws ListenerException MAY be thrown if a listener throws an exception.
     * @param interrupt {@code true} If the computation should be interrupted in the case that it has already commenced.
     * @return {@code true} If the promise was cancelled, {@code false} if it had already reached another terminal state.
     * @see Future#cancel(boolean)
     */
    @Override
    boolean cancel(final boolean interrupt) throws ListenerException;

    /**
     * @return {@code true} if any terminal method has been called.
     */
    @Override
    boolean isDone();

    /**
     * @return {@code true} if {@link Deferred#resolve(Object)} was the first terminal method called.
     */
    boolean isResolved();

    /**
     * @return {@code true} if {@link Deferred#reject(Throwable)} was  the first terminal method called.
     */
    boolean isRejected();

    /**
     * @return {@code true} if {@link #cancel(boolean)} was the first terminal method called.
     */
    @Override
    boolean isCancelled();

    /**
     * <p>Triggered when {@link Deferred#resolve(Object)} is the first terminal method called.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onResolve(final OnResolve<T> then);

    /**
     * <p>Triggered when {@link Deferred#reject(Throwable)} is the first terminal method called.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onReject(final OnReject<F> then);

    /**
     * <p>Triggered when {@link #cancel(boolean)} is the first terminal method called.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onCancel(final OnCancel then);

    /**
     * <p>Triggered on any event after which {@link #isDone()} will return {@code true}.</p>
     *
     * <p>Will be fired in addition to the callback for the specific event.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onComplete(final OnComplete then);

    /**
     * <p>Triggered when {@link Deferred#progress(Object)} is called.</p>
     *
     * <p>An implementation MUST ensure that the value from a call to {@link Deferred#progress(Object)} is reported to
     * every listener that is registered prior to that call. Values from calls to {@link Deferred#progress(Object)}
     * before this method was called MAY be sent to the listener. If they are they MUST be sent in the same order
     * they were received by {@link Deferred#progress(Object)};</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onProgress(final OnProgress<P> then);

    /**
     * <p>Triggered when {@link #get(long, java.util.concurrent.TimeUnit)} or {@link #get()} is called.
     * It will be called after this promise has transitioned into a state
     * where {@link #isDone()} will return {@code true}.</p>
     *
     * <p>Each get method will call the corresponding get method on the {@link Future}
     * in the thread that called either {@link #get()} or {@link #get(long, java.util.concurrent.TimeUnit)}.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onGet(final Future<?> then);
}
