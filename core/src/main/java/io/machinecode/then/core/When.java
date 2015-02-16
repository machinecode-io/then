package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.Promise;

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class When {

    public static <T,F,P> Deferred<T,F,P> deferred() {
        return new DeferredImpl<>();
    }

    public static <T,F,P> Deferred<T,F,P> resolved(final T that) {
        return new ResolvedDeferred<>(that);
    }

    public static <T,F,P> Deferred<T,F,P> rejected(final F failure) {
        return new RejectedDeferred<>(failure);
    }

    public static <T,F,P> Deferred<T,F,P> cancelled() {
        return new CancelledDeferred<>(true);
    }

    public static <T,F,P> Deferred<T,F,P> any(final Collection<? extends Promise<T,?,?>> promises) {
        return new AnyDeferred<>(promises);
    }

    public static <T,F,P> Deferred<T,F,P> any(final Promise<T,?,?>... promises) {
        return new AnyDeferred<>(promises);
    }

    public static <T,F,P> Deferred<List<T>,F,P> all(final Collection<? extends Promise<T,F,?>> promises) {
        return new AllDeferred<>(promises);
    }

    public static <T,F,P> Deferred<List<T>,F,P> all(final Promise<T,F,?>... promises) {
        return new AllDeferred<>(promises);
    }

    public static <T,F,P> Deferred<List<T>,F,P> some(final Collection<? extends Promise<T,?,?>> promises) {
        return new SomeDeferred<>(promises);
    }

    public static <T,F,P> Deferred<List<T>,F,P> some(final Promise<T,?,?>... promises) {
        return new SomeDeferred<>(promises);
    }

    public static <T,F,P> Deferred<T,F,P> when(final Collection<? extends Promise<?,?,?>> promises) {
        return new WhenDeferred<>(promises);
    }

    public static <T,F,P> Deferred<T,F,P> when(final Promise<?,?,?>... promises) {
        return new WhenDeferred<>(promises);
    }
}
