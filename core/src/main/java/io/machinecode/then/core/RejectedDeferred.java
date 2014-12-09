package io.machinecode.then.core;

/**
 * <p>A promise that is set to the {@link #REJECTED} terminal state when constructed.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class RejectedDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    public RejectedDeferred(final F failure) {
        reject(failure);
    }
}
