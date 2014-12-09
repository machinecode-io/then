package io.machinecode.then.api;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <p>Factory methods for different types of {@link io.machinecode.then.api.Promise}'s.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface Promises {

    <T,F,P> Deferred<T,F,P> deferred();

    <T,P> ExecutablePromise<T,Throwable,P> executable(final Callable<T> call);

    <T,P> ExecutablePromise<T,Throwable,P> executable(final Runnable call, final T value);

    <T,P> ExecutablePromise<T,Throwable,P> executable(final Future<T> future);

    <T,P> ExecutablePromise<T,Throwable,P> executable(final Future<T> future, final long timeout, final TimeUnit unit);

    <T,F,P> Promise<T,F,P> all(final Collection<? extends Promise<?,?,?>> promises);

    <T,F,P> Promise<T,F,P> all(final Promise<?,?,?>... promises);

    <T,F,P> Promise<T,F,P> any(final Collection<? extends Promise<?,?,?>> promises);

    <T,F,P> Promise<T,F,P> any(final Promise<?,?,?>... promises);

    <T,F,P> Promise<T,F,P> resolved(final T that);

    <T,F,P> Promise<T,F,P> rejected(final F fail);

    <T,F,P> Promise<T,F,P> cancelled(final boolean interrupt);
}
