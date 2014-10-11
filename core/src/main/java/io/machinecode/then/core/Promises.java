package io.machinecode.then.core;

import io.machinecode.then.api.Promise;

import java.util.Collection;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 */
public class Promises {

    public static <T,F extends Throwable> Promise<T,F> basic() {
        return new PromiseImpl<T, F>();
    }

    public static <T,F extends Throwable> Promise<T,F> all(final Collection<Promise<?, ?>> promises) {
        return new AllPromise<T, F>(promises);
    }

    public static <T,F extends Throwable> Promise<T,F> all(final Promise<?, ?>... promises) {
        return new AllPromise<T, F>(promises);
    }

    public static <T> Promise<T,Throwable> any(final Collection<Promise<?, ?>> promises) {
        return new AnyPromise<T>(promises);
    }

    public static <T> Promise<T,Throwable> any(final Promise<?, ?>... promises) {
        return new AnyPromise<T>(promises);
    }

    public static <T,F extends Throwable> Promise<T,F> resolved(final T that) {
        return new ResolvedPromise<T, F>(that);
    }

    public static <T,F extends Throwable> Promise<T,F> rejected(final F fail) {
        return new RejectedPromise<T, F>(fail);
    }

    public static <T,F extends Throwable> Promise<T,F> cancelled(final boolean interrupt) {
        return new CancelledPromise<T, F>(interrupt);
    }
}
