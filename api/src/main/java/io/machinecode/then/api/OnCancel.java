package io.machinecode.then.api;

/**
 * Listener for a {@link Promise} entering a {@link Promise#CANCELLED} terminal state.
 *
 * @see Promise
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface OnCancel {

    /**
     * @param mayInterrupt If a running computation may be interrupted.
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    boolean cancel(final boolean mayInterrupt);
}
