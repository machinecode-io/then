package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.Sync;
import io.machinecode.then.api.Await;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class PromiseImpl<T> implements Promise<T> {

    private static final Logger log = Logger.getLogger(PromiseImpl.class);

    protected final Sync lock = new SyncImpl(new ReentrantLock());

    protected volatile int state = PENDING;

    protected volatile T value;
    protected volatile Throwable failure;

    protected final List<OnResolve<T>> onResolves = new LinkedList<OnResolve<T>>();
    protected final List<OnReject<Throwable>> onRejects = new LinkedList<OnReject<Throwable>>();
    protected final List<OnComplete> onCompletes = new LinkedList<OnComplete>();

    protected final AtomicBoolean listLock = new AtomicBoolean(false);

    protected boolean setValue(final T value) {
        if (this.isDone()) {
            return true;
        }
        this.value = value;
        this.state = RESOLVED;
        return false;
    }

    protected boolean setFailure(final Throwable failure) {
        if (this.isDone()) {
            return true;
        }
        this.state = REJECTED;
        this.failure = failure;
        return false;
    }

    @Override
    public void resolve(final T value) {
        log().tracef(getResolveLogMessage());
        while (!lock.tryLock()) {}
        try {
            if (setValue(value)) {
                return;
            }
        } finally {
            lock.unlock();
        }
        Throwable exception = null;
        while (!listLock.compareAndSet(false, true)) {}
        try {
            for (final OnResolve<T> then : onResolves) {
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
            for (final OnComplete then : onCompletes) {
                try {
                    then.complete();
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
            signalAll();
        }
    }

    @Override
    public void reject(final Throwable failure) {
        log().tracef(failure, getRejectLogMessage());
        while (!lock.tryLock()) {}
        try {
            if (setFailure(failure)) {
                return;
            }
        } finally {
            lock.unlock();
        }
        while (!listLock.compareAndSet(false, true)) {}
        try {
            for (final OnReject<Throwable> then : onRejects) {
                try {
                    then.reject(this.failure);
                } catch (final Throwable e) {
                    failure.addSuppressed(e);
                }
            }
            for (final OnComplete then : onCompletes) {
                try {
                    then.complete();
                } catch (final Throwable e) {
                    failure.addSuppressed(e);
                }
            }
        } finally {
            listLock.set(false);
        }
        signalAll();
    }

    @Override
    public boolean isDone() {
        while (!lock.tryLock()) {}
        try {
            switch (this.state) {
                case REJECTED:
                case RESOLVED:
                    return true;
                default:
                    return false;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isResolved() {
        while (!lock.tryLock()) {}
        try {
            return this.state == RESOLVED;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isRejected() {
        while (!lock.tryLock()) {}
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
    public Promise<T> onResolve(final OnResolve<T> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onResolve"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
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
                        onResolves.add(then);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            then.resolve(this.value);
        }
        return this;
    }

    @Override
    public Promise<T> onReject(final OnReject<Throwable> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onReject"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
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
                        onRejects.add(then);
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

    @Override
    public PromiseImpl<T> onComplete(final OnComplete then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onComplete"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
        try {
            switch (this.state) {
                case REJECTED:
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        onCompletes.add(then);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            then.complete();
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
        while (!this.isDone()) {
            while (!this.lock.tryLock()) {}
            try {
                this.lock.enlist(lock);
            } finally {
                this.lock.unlock();
            }
            while (!lock.tryLock()) {}
            try {
                lock.await();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void await(final long timeout, final TimeUnit unit, final Sync lock) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final long timeoutMillis = unit.toMillis(timeout);
        final long end = System.currentTimeMillis() + timeoutMillis;
            while (!this.isDone()) {
                final long nextTimeout = _tryTimeout(end);
                while (!this.lock.tryLock()) {}
                try {
                    this.lock.enlist(lock);
                } finally {
                    this.lock.unlock();
                }
                while (!lock.tryLock()) {}
                try {
                    lock.await(nextTimeout, MILLISECONDS);
                } finally {
                    lock.unlock();
                }
            }
    }

    @Override
    public void signal() {
        while (!lock.tryLock()) {}
        try {
            lock.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void signalAll() {
        while (!lock.tryLock()) {}
        try {
            lock.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        while (!lock.tryLock()) {}
        try {
            switch (this.state) {
                case REJECTED:
                    throw new ExecutionException(getRejectLogMessage(), failure);
                case RESOLVED:
                    return value;
                //default/PENDING means this thread was notified before the computation actually completed
            }
            for (;;) {
                lock.await();
                switch (this.state) {
                    case REJECTED:
                        throw new ExecutionException(getRejectLogMessage(), failure);
                    case RESOLVED:
                        return value;
                    //default/PENDING means this thread was notified before the computation actually completed
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final long timeoutMillis = unit.toMillis(timeout);
        final long end = System.currentTimeMillis() + timeoutMillis;
        while (!lock.tryLock()) {}
        try {
            switch (this.state) {
                case REJECTED:
                    throw new ExecutionException(getRejectLogMessage(), failure);
                case RESOLVED:
                    return value;
                //default/PENDING means this thread was notified before the computation actually completed
            }
            for (;;) {
                long nextTimeout = _tryTimeout(end);
                if (!lock.await(nextTimeout, MILLISECONDS)) {
                    throw new TimeoutException(getTimeoutExceptionMessage());
                }
                switch (this.state) {
                    case REJECTED:
                        throw new ExecutionException(getRejectLogMessage(), failure);
                    case RESOLVED:
                        return value;
                    //default/PENDING means this thread was notified before the computation actually completed
                }
            }
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
