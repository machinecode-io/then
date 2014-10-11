package io.machinecode.then.api;

/**
 * <p>Listener for a {@link Deferred} entering a {@link Deferred#REJECTED} state.</p>
 *
 * @see Deferred
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface OnReject<F extends Throwable> {

    /**
     * @param fail The exception thrown by the computation represented by this promise.
     */
    void reject(final F fail);
}
