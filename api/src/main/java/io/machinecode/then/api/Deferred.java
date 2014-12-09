package io.machinecode.then.api;

import java.util.concurrent.Future;

/**
 * <p>A representation of a computation allowing listeners to be notified of state changes. This representation allows
 * for four main states: {@link #PENDING}, {@link #RESOLVED}, {@link #REJECTED} and {@link #CANCELLED}. Of these
 * {@link #RESOLVED}, {@link #REJECTED} and {@link #CANCELLED} will be referred to as 'terminal states', that is a
 * state when the {@link #isDone()} method will return {@code true}. A promise reaches one of these states by a call
 * to a 'terminal method', one of {@link #resolve(Object)}, {@link #reject(Object)} and {@link #cancel(boolean)}
 * respectively.</p>
 *
 * <p>The javadoc here only considers these three terminal states however this definition does not preclude inheritors
 * from adding further terminal states and associated methods.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface Deferred<T,F,P> extends OnResolve<T>, OnReject<F>, OnProgress<P>, Promise<T,F,P> {

    /**
     * <p>This is a transient state indicating no terminal method has yet been called.</p>
     */
    byte PENDING   = 0;
    /**
     * <p>This is a terminal state indicating {@link #resolve(Object)} was the first terminal method called.</p>
     */
    byte RESOLVED  = 1;
    /**
     * <p>This is a terminal state indicating {@link #reject(Object)} was the first terminal method called.</p>
     */
    byte REJECTED  = 2;
    /**
     * <p>This is a terminal state indicating {@link #cancel(boolean)} was the first terminal method called.</p>
     */
    byte CANCELLED = 3;

    /**
     * <p>Produces a readonly view of this Deferred.</p>
     *
     * @return A promise which will have it's listeners invoked when this Deferred reaches a terminal state.
     */
    Promise<T,F,P> promise();

    /**
     * <p>Called to indicate the successful completion of the computation this deferred represents. After this method has
     * been called {@link #isDone()} will return {@code true}. If this was the first terminal method to be called
     * {@link #isResolved()} will also return {@code true}.</p>
     *
     * @param that The result of the computation.
     * @throws ListenerException MAY be thrown if a listener throws an exception.
     * @throws ResolvedException MAY be thrown by an implementation if resolve has previously called.
     * @throws RejectedException MAY be thrown by an implementation if {@link #reject(Object)} has previously called.
     * @throws CancelledException MAY be thrown by an implementation if {@link #cancel(boolean)} has previously called.
     */
    @Override
    void resolve(final T that) throws ListenerException, ResolvedException, RejectedException, CancelledException;

    /**
     * <p>Called to indicate the failure of the computation this promise represents. After this method has
     * been called {@link #isDone()} will return {@code true}. If this was the first terminal method to be called
     * {@link #isRejected()} will also return {@code true}.</p>
     *
     * @param that The exception that caused the computation to terminate.
     * @throws ListenerException MAY be thrown if a listener throws an exception.
     * @throws ResolvedException MAY be thrown by an implementation if {@link #resolve(Object)} has previously called.
     * @throws RejectedException MAY be thrown by an implementation if reject has previously called.
     * @throws CancelledException MAY be thrown by an implementation if {@link #cancel(boolean)} has previously called.
     */
    @Override
    void reject(final F that) throws ListenerException, ResolvedException, RejectedException, CancelledException;

    /**
     * <p>Called to notify listeners that the some work has been done in the computation.</p>
     *
     * @param that The value to notify listeners with.
     * @throws ListenerException MAY be thrown if a listener throws an exception.
     */
    @Override
    void progress(final P that) throws ListenerException;


    /**
     * {@inheritDoc}
     */
    Deferred<T,F,P> onResolve(final OnResolve<T> then);

    /**
     * {@inheritDoc}
     */
    Deferred<T,F,P> onReject(final OnReject<F> then);

    /**
     * {@inheritDoc}
     */
    Deferred<T,F,P> onCancel(final OnCancel then);

    /**
     * {@inheritDoc}
     */
    Deferred<T,F,P> onComplete(final OnComplete then);

    /**
     * {@inheritDoc}
     */
    Deferred<T,F,P> onProgress(final OnProgress<P> then);

    /**
     * {@inheritDoc}
     */
    Deferred<T,F,P> onGet(final Future<?> then);
}
