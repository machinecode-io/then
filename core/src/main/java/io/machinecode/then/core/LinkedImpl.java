package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.Linked;
import io.machinecode.then.api.On;
import io.machinecode.then.api.Sync;
import org.jboss.logging.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class LinkedImpl<T> extends BaseLinked<T> {

    private static final Logger log = Logger.getLogger(LinkedImpl.class);

    protected volatile Linked<?> link;

    @Override
    public LinkedImpl<T> onLink(final On<Deferred<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onLink"));
        }
        lock.lock();
        try {
            if (this.link != null) {
                try {
                    on.on(this.link);
                } catch (final Throwable e) {
                    final RuntimeException exception = new RuntimeException(Messages.format("THEN-000200.linked.link.exception"), e);
                    log().warnf(exception, Messages.format("THEN-000200.linked.link.exception"));
                    throw exception;
                }
            } else {
                while (!listLock.compareAndSet(false, true)) {}
                try {
                    linkListeners.add(on);
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
        lock.lock();
        try {
            while (link == null) {
                this.lock.lock();
                try {
                    this.lock.enlist(lock);
                } finally {
                    this.lock.unlock();
                }
                lock.await();
            }
            try {
                if (link != null) {
                    link.await(lock);
                }
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
        lock.lock();
        try {
            while (link == null) {
                final long nextTimeout = _tryTimeout(end);
                this.lock.lock();
                try {
                    this.lock.enlist(lock);
                } finally {
                    this.lock.unlock();
                }
                lock.await(nextTimeout, MILLISECONDS);
            }
            try {
                final long nextTimeout = _tryTimeout(end);
                if (link != null) {
                    link.await(nextTimeout, MILLISECONDS, lock);
                }
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
    public LinkedImpl<T> link(final Linked<?> that) {
        if (that == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "link"));
        }
        lock.lock();
        try {
            link = that;
            RuntimeException exception = null;
            while (!listLock.compareAndSet(false, true)) {}
            try {
                for (final On<Deferred<?>> on : linkListeners) {
                    try {
                        on.on(that);
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
            lock.unlock();
        }
        return this;
    }

    @Override
    protected Logger log() {
        return log;
    }
}
