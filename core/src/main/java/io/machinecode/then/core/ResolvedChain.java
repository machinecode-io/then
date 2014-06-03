package io.machinecode.then.core;

import io.machinecode.then.api.Chain;
import io.machinecode.then.api.OnLink;
import io.machinecode.then.api.Sync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ResolvedChain<T> extends BaseChain<T> {

    public ResolvedChain(final T value) {
        resolve(value);
    }

    @Override
    public ResolvedChain<T> link(final Chain<?> that) {
        throw new IllegalStateException(); //TODO Message This is a terminal link
    }

    @Override
    public ResolvedChain<T> onLink(final OnLink then) {
        throw new IllegalStateException(); //TODO Message This is a terminal link
    }

    @Override
    public void await(final Sync lock) throws InterruptedException {
        while (!lock.tryLock()) {}
        try {
            lock.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void await(final long timeout, final TimeUnit unit, final Sync lock) throws InterruptedException, TimeoutException {
        while (!lock.tryLock()) {}
        try {
            lock.signal();
        } finally {
            lock.unlock();
        }
    }
}
