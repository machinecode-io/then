package io.machinecode.then.api;

/**
 * <p>Listener for a {@link Deferred} entering a {@link Deferred#RESOLVED} state.</p>
 *
 * @see Deferred
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface OnResolve<T> {

    /**
     * @param that The result of the computation represented by this promise.
     */
    void resolve(final T that);
}
