package io.machinecode.then.core;

import io.machinecode.then.api.Chain;
import io.machinecode.then.api.OnLink;
import io.machinecode.then.api.Sync;
import org.jboss.logging.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ChainImpl<T> extends BaseChain<T> {

    private static final Logger log = Logger.getLogger(ChainImpl.class);

    protected volatile Chain<?> link;

    @Override
    public ChainImpl<T> link(final Chain<?> that) {
        if (that == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "link"));
        }
        while (!lock.tryLock()) {}
        try {
            this.link = that;
            RuntimeException exception = null;
            while (!listLock.compareAndSet(false, true)) {}
            try {
                for (final OnLink on : onLinks) {
                    try {
                        on.link(that);
                    } catch (final Throwable e) {
                        if (exception == null) {
                            exception = new RuntimeException(Messages.format("THEN-000107.deferred.get.exception"), e);
                        } else {
                            exception.addSuppressed(e);
                        }
                    }
                }
            } finally {
                listLock.set(false);
            }
            if (exception != null) {
                log().warnf(exception, Messages.format("THEN-000107.deferred.get.exception"));
                throw exception;
            }
        } finally {
            try {
                lock.signalAll();
            } finally {
                lock.unlock();
            }
        }
        return this;
    }

    @Override
    public ChainImpl<T> onLink(final OnLink then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onLink"));
        }
        while (!lock.tryLock()) {}
        try {
            if (this.link != null) {
                try {
                    then.link(this.link);
                } catch (final Throwable e) {
                    final RuntimeException exception = new RuntimeException(Messages.format("THEN-000200.linked.link.exception"), e);
                    log().warnf(exception, Messages.format("THEN-000200.linked.link.exception"));
                    throw exception;
                }
            } else {
                while (!listLock.compareAndSet(false, true)) {}
                try {
                    onLinks.add(then);
                } finally {
                    listLock.set(false);
                }
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public void await(final Sync lock) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        while (!lock.tryLock()) {}
        try {
            while (this.link == null) {
                while (!this.lock.tryLock()) {}
                try {
                    this.lock.enlist(lock);
                } finally {
                    this.lock.unlock();
                }
                lock.await();
            }
            try {
                this.link.await(lock);
            } catch (final Throwable e) {
                final RuntimeException exception = new RuntimeException(Messages.format("THEN-000107.deferred.get.exception"), e);
                log().warnf(exception, Messages.format("THEN-000107.deferred.get.exception"));
                throw exception;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void await(final long timeout, final TimeUnit unit, final Sync lock) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final long timeoutMillis = unit.toMillis(timeout);
        final long end = System.currentTimeMillis() + timeoutMillis;
        while (!lock.tryLock()) {}
        try {
            while (this.link == null) {
                final long nextTimeout = _tryTimeout(end);
                while (!this.lock.tryLock()) {}
                try {
                    this.lock.enlist(lock);
                } finally {
                    this.lock.unlock();
                }
                lock.await(nextTimeout, MILLISECONDS);
            }
            try {
                final long nextTimeout = _tryTimeout(end);
                this.link.await(nextTimeout, MILLISECONDS, lock);
            } catch (final TimeoutException e) {
                throw e;
            } catch (final Throwable e) {
                final RuntimeException exception = new RuntimeException(Messages.format("THEN-000107.deferred.get.exception"), e);
                log().warnf(exception, Messages.format("THEN-000107.deferred.get.exception"));
                throw exception;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected Logger log() {
        return log;
    }
}
