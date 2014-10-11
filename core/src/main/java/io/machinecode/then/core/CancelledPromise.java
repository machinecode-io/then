package io.machinecode.then.core;

/**
 * A promise that is set to the {@link #CANCELLED} terminal state when constructed.
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class CancelledPromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    public CancelledPromise(final boolean interrupt) {
        cancel(interrupt);
    }
}
