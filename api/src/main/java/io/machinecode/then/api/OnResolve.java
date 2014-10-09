package io.machinecode.then.api;

/**
 * Listener for a {@link Promise} entering a {@link Promise#RESOLVED} state.
 *
 * @see Promise
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 * @since 1.0
 */
public interface OnResolve<T> {

    /**
     * @param that The result of the computation represented by this promise.
     */
    void resolve(final T that);
}
