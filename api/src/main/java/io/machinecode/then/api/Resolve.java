package io.machinecode.then.api;

/**
 * Interceptor allowing modification of chained {@link Promise}'s.
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface Resolve<T,Tx,Fx,Px> {

    /**
     * <p>The caller MUST ensure that one of {@link Deferred#resolve(Object)},
     * {@link Deferred#reject(Object)} or {@link Deferred#cancel(boolean)} is called on
     * the {@link Deferred} parameter.</p>
     *
     * @param that The resolved value of the original {@link Promise}.
     * @param next A {@link Deferred} that controls the state of the returned promise.
     * @see Promise#then(Resolve)
     * @see Deferred#resolve(Object)
     */
    void resolve(final T that, final Deferred<Tx, Fx, Px> next);
}
