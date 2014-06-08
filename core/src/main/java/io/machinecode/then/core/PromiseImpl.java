package io.machinecode.then.core;

import io.machinecode.then.api.ListenerException;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class PromiseImpl<T> implements Promise<T> {

    private static final Logger log = Logger.getLogger(PromiseImpl.class);

    protected volatile int state = PENDING;

    protected volatile T value;
    protected volatile Throwable failure;

    protected final List<OnResolve<T>> onResolves = new LinkedList<OnResolve<T>>();
    protected final List<OnReject<Throwable>> onRejects = new LinkedList<OnReject<Throwable>>();
    protected final List<OnComplete> onCompletes = new LinkedList<OnComplete>();

    private final AtomicBoolean guard = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    protected void _in() {
        while (!guard.compareAndSet(false, true)) {}
    }

    protected void _out() {
        guard.set(false);
    }

    protected void _lock() {
        while (!lock.tryLock()) {}
    }

    protected void _unlock() {
        lock.unlock();
    }

    protected void _await() throws InterruptedException {
        condition.await();
    }

    protected void _await(final long timeout, final TimeUnit unit) throws InterruptedException {
        condition.await(timeout, unit);
    }

    protected void _signalAll() {
        condition.signalAll();
    }

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
        _lock();
        try {
            if (setValue(value)) {
                return;
            }
        } finally {
            _unlock();
        }
        Throwable exception = null;
        _in();
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
            _out();
        }
        _lock();
        try {
            _signalAll();
        } finally {
            _unlock();
        }
        if (exception != null) {
            throw new ListenerException(exception);
        }
    }

    @Override
    public void reject(final Throwable failure) {
        log().tracef(failure, getRejectLogMessage());
        _lock();
        try {
            if (setFailure(failure)) {
                return;
            }
        } finally {
            _unlock();
        }
        _in();
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
            _out();
        }
        _lock();
        try {
            _signalAll();
        } finally {
            _unlock();
        }
    }

    @Override
    public boolean isDone() {
        switch (this.state) {
            case REJECTED:
            case RESOLVED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isResolved() {
        return this.state == RESOLVED;
    }

    @Override
    public boolean isRejected() {
        return this.state == REJECTED;
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
        _lock();
        try {
            switch (this.state) {
                case REJECTED:
                    return this;
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    _in();
                    try {
                        onResolves.add(then);
                    } finally {
                        _out();
                    }
            }
        } finally {
            _unlock();
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
        _lock();
        try {
            switch (this.state) {
                case RESOLVED:
                    return this;
                case REJECTED:
                    run = true;
                case PENDING:
                default:
                    _in();
                    try {
                        onRejects.add(then);
                    } finally {
                        _out();
                    }
            }
        } finally {
            _unlock();
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
        _lock();
        try {
            switch (this.state) {
                case REJECTED:
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    _in();
                    try {
                        onCompletes.add(then);
                    } finally {
                        _out();
                    }
            }
        } finally {
            _unlock();
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
    public T get() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        _lock();
        try {
            switch (this.state) {
                case REJECTED:
                    throw new ExecutionException(getRejectLogMessage(), failure);
                case RESOLVED:
                    return value;
                //default/PENDING means this thread was notified before the computation actually completed
            }
            for (;;) {
                condition.await();
                switch (this.state) {
                    case REJECTED:
                        throw new ExecutionException(getRejectLogMessage(), failure);
                    case RESOLVED:
                        return value;
                    //default/PENDING means this thread was notified before the computation actually completed
                }
            }
        } finally {
            _unlock();
        }
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final long end = System.currentTimeMillis() + unit.toMillis(timeout);
        _lock();
        try {
            switch (this.state) {
                case REJECTED:
                    throw new ExecutionException(getRejectLogMessage(), failure);
                case RESOLVED:
                    return value;
                //default/PENDING means this thread was notified before the computation actually completed
            }
            for (;;) {
                condition.await(_tryTimeout(end), MILLISECONDS);
                switch (this.state) {
                    case REJECTED:
                        throw new ExecutionException(getRejectLogMessage(), failure);
                    case RESOLVED:
                        return value;
                    //default/PENDING means this thread was notified before the computation actually completed
                }
            }
        } finally {
            _unlock();
        }
    }

    protected long _tryTimeout(final long end) throws TimeoutException {
        final long timeout = end - System.currentTimeMillis();
        if (timeout <= 0) {
            throw new TimeoutException(getTimeoutExceptionMessage());
        }
        return timeout;
    }
}
