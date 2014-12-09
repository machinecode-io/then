package io.machinecode.then.core;

/**
 * <p>A promise that is set to the {@link #CANCELLED} terminal state when constructed.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class CancelledDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    public CancelledDeferred(final boolean interrupt) {
        cancel(interrupt);
    }
}
