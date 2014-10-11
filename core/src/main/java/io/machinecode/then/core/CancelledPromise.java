package io.machinecode.then.core;

/**
 * <p>A promise that is set to the {@link #CANCELLED} terminal state when constructed.</p>
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class CancelledPromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    public CancelledPromise(final boolean interrupt) {
        cancel(interrupt);
    }
}
