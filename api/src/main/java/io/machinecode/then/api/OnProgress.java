package io.machinecode.then.api;

/**
 * Listener for a {@link Deferred#progress(Object)}
 *
 * @see Deferred
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface OnProgress<P> {

    /**
     * @param that The value representing progress of the computation.
     */
    void progress(final P that);
}
