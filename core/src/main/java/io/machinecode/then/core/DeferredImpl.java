package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.ListenerException;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
            for (final OnResolve<T> on : onResolves) {
                try {
                    on.resolve(this.value);
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
            _out();
        }
        _lock();
        try {
            _signalAll();
        } finally {
            _unlock();
        }
        if (exception != null) {
            throw new ListenerException(exception); //TODO Should this be changed to rejected?
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
            for (final OnComplete on : onCompletes) {
                try {
                    on.complete();
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

    protected boolean checkCancelled(final boolean futureCompatible) {
        return this.isDone();
    }

    @Override
    public void cancel() {
        _lock();
        try {
            doCancel(false);
        } finally {
            _unlock();
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        _lock();
        try {
            doCancel(true);
            return isCancelled();
        } finally {
            _unlock();
        }
    }

    protected void doCancel(final boolean futureCompatible) {
        log().tracef(getCancelLogMessage());
        ListenerException exception = null;
        _lock();
        try {
            if (this.checkCancelled(futureCompatible)) {
                return;
            }
            this.state = CANCELLED;
        } finally {
            _unlock();
        }
        _in();
        try {
            for (final OnCancel then : onCancels) {
                try {
                    then.cancel();
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = new ListenerException(Messages.format("THEN-000108.deferred.cancel.exception"), e);
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
                        exception = new ListenerException(Messages.format("THEN-000108.deferred.cancel.exception"), e);
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        } finally {
            _out();
            _lock();
            try {
                _signalAll();
            } finally {
                _unlock();
            }
        }
    }

    @Override
    public boolean isDone() {
        switch (this.state) {
            case RESOLVED:
            case REJECTED:
            case CANCELLED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isCancelled() {
        return this.state == CANCELLED;
    }

    @Override
    public boolean isRejected() {
        return this.state == REJECTED;
    }

    @Override
    public boolean isResolved() {
        return this.state == RESOLVED;
    }

    @Override
    public DeferredImpl<T> onResolve(final OnResolve<T> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onResolve"));
        }
        boolean run = false;
        _lock();
        try {
            switch (this.state) {
                case REJECTED:
                case CANCELLED:
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
    public DeferredImpl<T> onReject(final OnReject<Throwable> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onReject"));
        }
        boolean run = false;
        _lock();
        try {
            switch (this.state) {
                case RESOLVED:
                case CANCELLED:
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
    public DeferredImpl<T> onCancel(final OnCancel then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onCancel"));
        }
        boolean run = false;
        _lock();
        try {
            switch (this.state) {
                case RESOLVED:
                case REJECTED:
                    return this;
                case CANCELLED:
                    run = true;
                case PENDING:
                default:
                    _in();
                    try {
                        onCancels.add(then);
                    } finally {
                        _out();
                    }
            }
        } finally {
            _unlock();
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
        _lock();
        try {
            switch (this.state) {
                case REJECTED:
                case CANCELLED:
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

    @Override
    public T get() throws InterruptedException, ExecutionException, CancellationException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        _lock();
        try {
            switch (this.state) {
                case CANCELLED:
                    throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                case REJECTED:
                    throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
                case RESOLVED:
                    return value;
            }
            for (;;) {
                _await();
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
            _unlock();
        }
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final long end = System.currentTimeMillis() + unit.toMillis(timeout);
        _lock();
        try {
            switch (this.state) {
                case CANCELLED:
                    throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                case REJECTED:
                    throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
                case RESOLVED:
                    return value;
            }
            for (;;) {
                _await(_tryTimeout(end), MILLISECONDS);
                switch (this.state) {
                    case CANCELLED:
                        throw new CancellationException(Messages.format("THEN-000106.deferred.cancelled"));
                    case REJECTED:
                        throw new ExecutionException(Messages.format("THEN-000105.deferred.rejected"), failure);
                    case RESOLVED:
                        return value;
                }
                throw new TimeoutException(getTimeoutExceptionMessage());
            }
        } finally {
            _unlock();
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
}
