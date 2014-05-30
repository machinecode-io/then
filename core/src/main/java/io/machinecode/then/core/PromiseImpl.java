package io.machinecode.then.core;

import io.machinecode.then.api.On;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.Sync;
import io.machinecode.then.api.Synchronized;
import io.machinecode.then.api.WhenRejected;
import io.machinecode.then.api.WhenResolved;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class PromiseImpl<T> implements Promise<T>, Synchronized {

    private static final Logger log = Logger.getLogger(PromiseImpl.class);

    protected final Sync lock = new SyncImpl(new ReentrantLock());

    protected volatile int state = PENDING;

    protected volatile T value;
    protected volatile Throwable failure;

    protected final List<On<Promise<?>>> onResolves = new LinkedList<On<Promise<?>>>();
    protected final List<On<Promise<?>>> onRejects = new LinkedList<On<Promise<?>>>();
    protected final List<WhenResolved<T>> whenResolveds = new LinkedList<WhenResolved<T>>();
    protected final List<WhenRejected<Throwable>> whenRejecteds = new LinkedList<WhenRejected<Throwable>>();

    protected final AtomicBoolean listLock = new AtomicBoolean(false);

    @Override
    public void resolve(final T value) {
        log().tracef(getResolveLogMessage());
        lock.lock();
        try {
            this.value = value;
            if (this.state == PENDING) {
                this.state = RESOLVED;
            }
        } finally {
            lock.unlock();
        }
        Throwable exception = null;
        while (!listLock.compareAndSet(false, true)) {}
        try {
            for (final WhenResolved<T> then : whenResolveds) {
                try {
                    then.resolve(this.value);
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = e;
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
            for (final On<Promise<?>> on : onResolves) {
                try {
                    on.on(this);
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = e;
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
        } finally {
            listLock.set(false);
        }
        if (exception != null) {
            reject(exception);
        } else {
            signal();
        }
    }

    @Override
    public void reject(final Throwable failure) {
        log().tracef(failure, getRejectLogMessage());
        lock.lock();
        try {
            this.failure = failure;
            this.state = REJECTED;
        } finally {
            lock.unlock();
        }
        while (!listLock.compareAndSet(false, true)) {}
        try {
            for (final WhenRejected<Throwable> then : whenRejecteds) {
                try {
                    then.reject(this.failure);
                } catch (final Throwable e) {
                    failure.addSuppressed(e);
                }
            }
            for (final On<Promise<?>> on : onRejects) {
                try {
                    on.on(this);
                } catch (final Throwable e) {
                    failure.addSuppressed(e);
                }
            }
        } finally {
            listLock.set(false);
        }
        signal();
    }

    @Override
    public boolean isResolved() {
        lock.lock();
        try {
            return this.state == RESOLVED;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isRejected() {
        lock.lock();
        try {
            return this.state == REJECTED;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public Promise<T> onResolve(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onResolve"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case REJECTED:
                    return this;
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        onResolves.add(on);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            on.on(this);
        }
        return this;
    }

    @Override
    public Promise<T> onReject(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onReject"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case RESOLVED:
                    return this;
                case REJECTED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        onRejects.add(on);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            on.on(this);
        }
        return this;
    }

    @Override
    public PromiseImpl<T> always(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "always"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case REJECTED:
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        onResolves.add(on);
                        onRejects.add(on);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            on.on(this);
        }
        return this;
    }

    @Override
    public Promise<T> whenResolved(final WhenResolved<T> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "whenResolved"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case REJECTED:
                    return this;
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        whenResolveds.add(then);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            this.resolve(this.value);
        }
        return this;
    }

    @Override
    public Promise<T> whenRejected(final WhenRejected<Throwable> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "whenRejected"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case RESOLVED:
                    return this;
                case REJECTED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        whenRejecteds.add(then);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            then.reject(this.failure);
        }
        return this;
    }

    protected String getResolveLogMessage() {
        return Messages.get("THEN-000000.promise.resolve");
    }

    protected String getRejectLogMessage() {
        return Messages.get("THEN-000001.promise.reject");
    }

    protected String getCancelLogMessage() {
        return Messages.get("THEN-000002.promise.cancel");
    }

    protected String getTimeoutExceptionMessage() {
        return Messages.get("THEN-000003.promise.timeout");
    }

    protected String getInterruptedExceptionMessage() {
        return Messages.format("THEN-000004.promise.interrupted");
    }

    protected Logger log() {
        return log;
    }

    @Override
    public void await(final Sync lock) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        lock.lock();
        try {
            this.lock.lock();
            try {
                this.lock.enlist(lock);
            } finally {
                this.lock.unlock();
            }
            lock.await();
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
            final long nextTimeout = _tryTimeout(end);
            this.lock.lock();
            try {
                this.lock.enlist(lock);
            } finally {
                this.lock.unlock();
            }
            lock.await(nextTimeout, MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void signal() {
        lock.lock();
        try {
            lock.signalAll();
        } finally {
            lock.unlock();
        }
    }

    protected long _tryTimeout(final long end) throws TimeoutException {
        final long nextTimeout = end - System.currentTimeMillis();
        if (nextTimeout <= 0) {
            throw new TimeoutException(getTimeoutExceptionMessage());
        }
        return nextTimeout;
    }
}
