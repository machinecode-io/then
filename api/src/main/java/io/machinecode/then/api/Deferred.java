package io.machinecode.then.api;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Deferred<T> extends Future<T> {

    void resolve(final T that);

    void reject(final Throwable that);

    boolean isResolved();

    boolean isRejected();

    void then(final Cancel<T, Throwable> then);

    void then(final Cancel<T, Throwable> then, final long timeout, final TimeUnit unit);

    void always(final On<Deferred<?>> on);

    void onResolve(final On<Deferred<?>> on);

    void onReject(final On<Deferred<?>> on);

    void onCancel(final On<Deferred<?>> on);
}
