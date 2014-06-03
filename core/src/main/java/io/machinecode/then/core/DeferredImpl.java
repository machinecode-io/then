package io.machinecode.then.core;

import io.machinecode.then.api.Chain;
import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnLink;
import io.machinecode.then.api.Await;
import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
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
public class DeferredImpl<T> extends PromiseImpl<T> implements Deferred<T> {

    private static final Logger log = Logger.getLogger(DeferredImpl.class);

    protected final List<OnCancel> onCancels = new LinkedList<OnCancel>();

    @Override
    protected boolean setValue(final T value) {
        if (this.isDone()) {
            return true;
        }
        this.value = value;
        this.state = RESOLVED;
        return false;
    }

    @Override
    protected boolean setFailure(final Throwable failure) {
        if (this.isDone()) {
            return true;
        }
        this.failure = failure;
        this.state = REJECTED;
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
            for (final OnComplete on : onCompletes) {
                try {
                    on.complete();
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
            reject(exception); //TODO This is not right as state has already been set to RESOLVED
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
            for (final OnComplete on : onCompletes) {
                try {
                    on.complete();
                } catch (final Throwable e) {
                    failure.addSuppressed(e);
                }
            }
        } finally {
            listLock.set(false);
        }
        signalAll();
    }

    protected boolean checkCancelled(final boolean futureCompatible) {
        return this.isDone();
    }

    @Override
    public void cancel() {
        doCancel(false);
    }

    protected void doCancel(final boolean futureCompatible) {
        log().tracef(getCancelLogMessage());
        final CancelListener listener = new CancelListener();
        RuntimeException exception = null;
        while (!lock.tryLock()) {}
        try {
            cancelling(listener);
            if (listener.exception != null) {
                signalAll();
                throw listener.exception;
            }
            if (this.checkCancelled(futureCompatible)) {
                return;
            }
            this.state = CANCELLED;
        } finally {
            lock.unlock();
        }
        while (!listLock.compareAndSet(false, true)) {}
        try {
            for (final OnCancel then : onCancels) {
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
            for (final OnComplete on : onCompletes) {
                try {
                    on.complete();
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
        } finally {
            listLock.set(false);
            signalAll();
        }
    }

    @Override
    public boolean isDone() {
        while (!lock.tryLock()) {}
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
    public boolean cancel(final boolean mayInterruptIfRunning) {
        doCancel(true);
        return isCancelled();
    }

    @Override
    public boolean isCancelled() {
        while (!lock.tryLock()) {}
        try {
            return this.state == CANCELLED;
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
    public boolean isResolved() {
        while (!lock.tryLock()) {}
        try {
            return this.state == RESOLVED;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public DeferredImpl<T> onResolve(final OnResolve<T> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onResolve"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
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
    public DeferredImpl<T> onReject(final OnReject<Throwable> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onReject"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
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
    public DeferredImpl<T> onCancel(final OnCancel then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onCancel"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
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
                        onCancels.add(then);
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

    @Override
    public DeferredImpl<T> onComplete(final OnComplete then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onComplete"));
        }
        boolean run = false;
        while (!lock.tryLock()) {}
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

    protected void cancelling(final OnLink then) {
        // For inheritors
    }

    @Override
    public T get() throws InterruptedException, ExecutionException, CancellationException {
        while (!lock.tryLock()) {}
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
        while (!lock.tryLock()) {}
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

    private static final class CancelListener implements OnLink, Serializable {
        private RuntimeException exception = null;

        @Override
        public void link(final Chain<?> that) {
            try {
                that.cancel();
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
