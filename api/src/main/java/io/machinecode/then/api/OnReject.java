package io.machinecode.then.api;

/**
 * <p>Listener for a {@link Promise} entering a {@link Promise#REJECTED} state.</p>
 *
 * @see Promise
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface OnReject<F extends Throwable> {

    /**
     * @param fail The exception thrown by the computation represented by this promise.
     */
    void reject(final F fail);
}
