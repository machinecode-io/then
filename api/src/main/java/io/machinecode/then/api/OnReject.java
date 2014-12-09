package io.machinecode.then.api;

/**
 * <p>Listener for a {@link Deferred} entering a {@link Deferred#REJECTED} state.</p>
 *
 * @see Deferred
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface OnReject<F> {

    /**
     * @param fail The exception thrown by the computation represented by this promise.
     */
    void reject(final F fail);
}
