package io.machinecode.then.core;

import io.machinecode.then.api.CompletionException;
import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.Promise;

import java.util.Collection;

/**
 * <p>Factory methods for different types of {@link Promise}'s.</p>
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public final class Then {

    private Then() {}

    public static <T,F extends Throwable,P> Deferred<T,F,P> deferred() {
        return new DeferredImpl<>();
    }

    public static <T,F extends Throwable,P> Promise<T,F,P> all(final Collection<? extends Promise<?,?,?>> promises) {
        return new AllDeferred<>(promises);
    }

    public static <T,F extends Throwable,P> Promise<T,F,P> all(final Promise<?,?,?>... promises) {
        return new AllDeferred<>(promises);
    }

    public static <T,P> Promise<T,CompletionException,P> any(final Collection<? extends Promise<?,?,?>> promises) {
        return new AnyDeferred<>(promises);
    }

    public static <T,P> Promise<T,CompletionException,P> any(final Promise<?,?,?>... promises) {
        return new AnyDeferred<>(promises);
    }

    public static <T,F extends Throwable,P> Promise<T,F,P> resolved(final T that) {
        return new ResolvedDeferred<>(that);
    }

    public static <T,F extends Throwable,P> Promise<T,F,P> rejected(final F fail) {
        return new RejectedDeferred<>(fail);
    }

    public static <T,F extends Throwable,P> Promise<T,F,P> cancelled(final boolean interrupt) {
        return new CancelledDeferred<>(interrupt);
    }
}
