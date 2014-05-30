package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.On;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.Synchronized;
import io.machinecode.then.api.WhenCancelled;
import io.machinecode.then.api.WhenRejected;
import io.machinecode.then.api.WhenResolved;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class DeferredImpl<T> extends PromiseImpl<T> implements Deferred<T>, Synchronized {

    private static final Logger log = Logger.getLogger(DeferredImpl.class);

    protected final List<On<Promise<?>>> onCancels = new LinkedList<On<Promise<?>>>();
    protected final List<WhenCancelled> whenCancelleds = new LinkedList<WhenCancelled>();

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
            for (final On<Promise<?>> listener : onResolves) {
                try {
                    listener.on(this);
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
            if (this.state != CANCELLED) {
                this.state = REJECTED;
            }
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
            for (final On<Promise<?>> listener : onRejects) {
                try {
                    listener.on(this);
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
    public boolean cancel(final boolean mayInterruptIfRunning) {
        log().tracef(getCancelLogMessage());
        final CancelListener listener = new CancelListener(mayInterruptIfRunning);
        RuntimeException exception = null;
        lock.lock();
        try {
            cancelling(listener);
            if (listener.exception != null) {
                signal();
                throw listener.exception;
            }
            if (this.isCancelled()) {
                return true;
            }
            if (this.isResolved() || this.isRejected()) {
                return false;
            }
            this.state = CANCELLED;
        } finally {
            lock.unlock();
        }
        while (!listLock.compareAndSet(false, true)) {}
        try {
            for (final WhenCancelled then : whenCancelleds) {
                try {
                    then.cancel();
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = new RuntimeException(Messages.format("THEN-000108.deferred.cancel.exception"), e);
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
            for (final On<Promise<?>> on : onCancels) {
                try {
                    on.on(this);
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = new RuntimeException(Messages.format("THEN-000108.deferred.cancel.exception"), e);
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
            return listener.cancelled;
        } finally {
            listLock.set(false);
            signal();
        }
    }

    @Override
    public boolean isDone() {
        lock.lock();
        try {
            switch (this.state) {
                case RESOLVED:
                case REJECTED:
                case CANCELLED:
                    return true;
                default:
                    return false;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        lock.lock();
        try {
            return this.state == CANCELLED;
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
    public boolean isResolved() {
        lock.lock();
        try {
            return this.state == RESOLVED;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public DeferredImpl<T> onResolve(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onResolve"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case REJECTED:
                case CANCELLED:
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
    public DeferredImpl<T> onReject(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onReject"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case RESOLVED:
                case CANCELLED:
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
    public DeferredImpl<T> onCancel(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onCancel"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case RESOLVED:
                case REJECTED:
                    return this;
                case CANCELLED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        onCancels.add(on);
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
    public DeferredImpl<T> always(final On<Promise<?>> on) {
        if (on == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "always"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case REJECTED:
                case CANCELLED:
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        onResolves.add(on);
                        onRejects.add(on);
                        onCancels.add(on);
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
    public DeferredImpl<T> whenResolved(final WhenResolved<T> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "whenResolved"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case REJECTED:
                case CANCELLED:
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
            then.resolve(this.value);
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
                case CANCELLED:
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

    @Override
    public Deferred<T> whenCancelled(final WhenCancelled then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "whenCancelled"));
        }
        boolean run = false;
        lock.lock();
        try {
            switch (this.state) {
                case RESOLVED:
                case REJECTED:
                    return this;
                case CANCELLED:
                    run = true;
                case PENDING:
                default:
                    while (!listLock.compareAndSet(false, true)) {}
                    try {
                        whenCancelleds.add(then);
                    } finally {
                        listLock.set(false);
                    }
            }
        } finally {
            lock.unlock();
        }
        if (run) {
            then.cancel();
        }
        return this;
    }

    protected void cancelling(final On<Deferred<?>> on) {
        // For inheritors
    }

    @Override
    public T get() throws InterruptedException, ExecutionException, CancellationException {
        lock.lock();
        try {
            await(lock);
            switch (this.state) {
                case CANCELLED:
                    throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                case REJECTED:
                    throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
                case RESOLVED:
                    return value;
            }
            for (;;) {
                lock.await();
                switch (this.state) {
                    case CANCELLED:
                        throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                    case REJECTED:
                        throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
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
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
        lock.lock();
        try {
            await(timeout, unit, lock);
            switch (this.state) {
                case CANCELLED:
                    throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                case REJECTED:
                    throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
                case RESOLVED:
                    return value;
            }
            lock.await(timeout, unit);
            switch (this.state) {
                case CANCELLED:
                    throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                case REJECTED:
                    throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
                case RESOLVED:
                    return value;
            }
            throw new TimeoutException(getTimeoutExceptionMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected String getResolveLogMessage() {
        return Messages.get("THEN-000100.deferred.resolve");
    }

    @Override
    protected String getRejectLogMessage() {
        return Messages.get("THEN-000101.deferred.reject");
    }

    @Override
    protected String getCancelLogMessage() {
        return Messages.get("THEN-000102.deferred.cancel");
    }

    @Override
    protected String getTimeoutExceptionMessage() {
        return Messages.get("THEN-000103.deferred.timeout");
    }

    @Override
    protected String getInterruptedExceptionMessage() {
        return Messages.format("THEN-000104.deferred.interrupted");
    }

    @Override
    protected Logger log() {
        return log;
    }

    private static final class CancelListener implements On<Deferred<?>>, Serializable {
        private final boolean mayInterruptIfRunning;
        private boolean cancelled = true;
        private RuntimeException exception = null;

        private CancelListener(final boolean mayInterruptIfRunning) {
            this.mayInterruptIfRunning = mayInterruptIfRunning;
        }

        @Override
        public void on(final Deferred<?> that) {
            try {
                cancelled = that.cancel(mayInterruptIfRunning) && cancelled;
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new RuntimeException(Messages.format("THEN-000108.deferred.cancel.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
    }
}
