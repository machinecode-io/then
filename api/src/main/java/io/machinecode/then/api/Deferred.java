package io.machinecode.then.api;

import java.util.concurrent.Future;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Deferred<T> extends Promise<T>, Future<T>, OnCancel {

    int CANCELLED = 3;

    @Override
    void resolve(final T that) throws ResolvedException, RejectedException, CancelledException;

    @Override
    void reject(final Throwable that) throws ResolvedException, RejectedException, CancelledException;

    @Override
    void cancel() throws ResolvedException, RejectedException, CancelledException;

    @Override
    boolean isCancelled();

    Deferred<T> onCancel(final OnCancel then);
}
