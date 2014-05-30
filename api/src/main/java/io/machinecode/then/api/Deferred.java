package io.machinecode.then.api;

import java.util.concurrent.Future;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Deferred<T> extends Future<T>, Promise<T> {

    int CANCELLED = 3;

    Deferred<T> whenCancelled(final WhenCancelled then);

    Deferred<T> onCancel(final On<Promise<?>> on);
}
