package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.Linked;
import io.machinecode.then.api.On;
import io.machinecode.then.api.Sync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Resolved<T> extends BaseLinked<T> {

    public Resolved(final T value) {
        resolve(value);
    }

    @Override
    public Resolved<T> link(final Linked<?> that) {
        //no op
        return this;
    }

    @Override
    public Resolved<T> onLink(final On<Deferred<?>> listener) {
        //no op
        return this;
    }

    @Override
    public void await(final Sync lock) throws InterruptedException {
        lock.lock();
        try {
            lock.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void await(final long timeout, final TimeUnit unit, final Sync lock) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            lock.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
