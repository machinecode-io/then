package io.machinecode.then.core;

/**
 * <p>A promise that is set to the {@link #RESOLVED} terminal state when constructed.</p>
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class ResolvedDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    public ResolvedDeferred(final T value) {
        resolve(value);
    }
}
