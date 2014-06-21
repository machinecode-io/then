package io.machinecode.then.core;

import io.machinecode.then.api.ListenerException;
import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

    protected static final byte ON_RESOLVE     = 100;
    protected static final byte ON_REJECT      = 101;
    protected static final byte ON_CANCEL      = 102;
    protected static final byte ON_COMPLETE    = 103;
    protected static final byte ON_GET         = 104;

    protected volatile byte state = PENDING;

    protected volatile T value;
    protected volatile Throwable failure;

    private final AtomicBoolean _guard = new AtomicBoolean(false);
    private final ReentrantLock _lock = new ReentrantLock();
    private final Condition _condition = _lock.newCondition();

    private volatile Events[] _events;
    private volatile int _pos = 0;

    private static class Events {
        final byte event;
        final Object value;

        private Events(final byte event, final Object value) {
            this.event = event;
            this.value = value;
        }
    }

    protected void _addEvent(final byte event, final Object that) {
        while (!_guard.compareAndSet(false, true)) {}
        try {
            if (_pos >= _events.length) {
                final Events[] o = new Events[_events.length + 1];
                System.arraycopy(_events, 0, o, 0, _pos);
                _events = o;
            }
            _events[_pos++] = new Events(event, that);
        } finally {
            _guard.set(false);
        }
    }

    protected <X> List<X> _getEvents(final byte event) {
        final List<X> list = new LinkedList<X>();
        while (!_guard.compareAndSet(false, true)) {}
        try {
            for (int i = 0; i < _pos; ++i) {
                if (event == _events[i].event) {
                    list.add((X) _events[i].value);
                }
            }
        } finally {
            _guard.set(false);
        }
        return list;
    }

    public PromiseImpl() {
        this._events = new Events[2];
    }

    public PromiseImpl(final byte hint) {
        this._events = new Events[hint];
    }

    protected void _lock() {
        while (!_lock.tryLock()) {}
    }

    protected void _unlock() {
        _lock.unlock();
    }

    protected void _await() throws InterruptedException {
        _condition.await();
    }

    protected boolean _await(final long timeout, final TimeUnit unit) throws InterruptedException {
        return _condition.await(timeout, unit);
    }

    protected void _signalAll() {
        _condition.signalAll();
    }

    protected boolean setValue(final T value) {
        switch (this.state) {
            case RESOLVED:
            case REJECTED:
            case CANCELLED:
                return true;
        }
        this.value = value;
        this.state = RESOLVED;
        return false;
    }

    protected boolean setFailure(final Throwable failure) {
        switch (this.state) {
            case RESOLVED:
            case REJECTED:
            case CANCELLED:
                return true;
        }
        this.failure = failure;
        this.state = REJECTED;
        return false;
    }

    protected boolean setCancelled() {
        switch (this.state) {
            case CANCELLED:
            case REJECTED:
            case RESOLVED:
                return true;
        }
        this.state = CANCELLED;
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
        for (final OnResolve<T> on : this.<OnResolve<T>>_getEvents(ON_RESOLVE)) {
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
        for (final OnComplete on : this.<OnComplete>_getEvents(ON_COMPLETE)) {
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
        for (final OnReject<Throwable> then : this.<OnReject<Throwable>>_getEvents(ON_REJECT)) {
            try {
                then.reject(this.failure);
            } catch (final Throwable e) {
                failure.addSuppressed(e);
            }
        }
        for (final OnComplete on : this.<OnComplete>_getEvents(ON_COMPLETE)) {
            try {
                on.complete();
            } catch (final Throwable e) {
                failure.addSuppressed(e);
            }
        }
        _lock();
        try {
            _signalAll();
        } finally {
            _unlock();
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        log().tracef(getCancelLogMessage());
        ListenerException exception = null;
        _lock();
        try {
            if (setCancelled()) {
                return isCancelled();
            }
        } finally {
            _unlock();
        }
        for (final OnCancel then : this.<OnCancel>_getEvents(ON_CANCEL)) {
            try {
                then.cancel(mayInterruptIfRunning);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000013.promise.cancel.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        for (final OnComplete on : this.<OnComplete>_getEvents(ON_COMPLETE)) {
            try {
                on.complete();
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000013.promise.cancel.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        _lock();
        try {
            _signalAll();
        } finally {
            _unlock();
        }
        if (exception != null) {
            throw exception;
        }
        return true;
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
    public PromiseImpl<T> onResolve(final OnResolve<T> then) {
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
                    _addEvent(ON_RESOLVE, then);
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
    public PromiseImpl<T> onReject(final OnReject<Throwable> then) {
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
                    _addEvent(ON_REJECT, then);
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
    public PromiseImpl<T> onCancel(final OnCancel then) {
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
                    _addEvent(ON_CANCEL, then);
            }
        } finally {
            _unlock();
        }
        if (run) {
            then.cancel(true);
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
                case CANCELLED:
                case RESOLVED:
                    run = true;
                case PENDING:
                default:
                    _addEvent(ON_COMPLETE, then);
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
    public Promise<T> onGet(final Future<?> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onGet"));
        }
        _addEvent(ON_GET, then);
        return this;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        _lock();
        try {
            loop: do {
                switch (this.state) {
                    case CANCELLED:
                    case REJECTED:
                    case RESOLVED:
                        break loop;
                }
                _await();
                _signalAll();
            } while (true);
        } finally {
            _unlock();
        }
        // TODO This read should be fine so long as once state reached terminal state it is never changed
        switch (this.state) {
            case CANCELLED:
                throw _onGet(new CancellationException(Messages.format("THEN-000012.promise.cancelled")));
            case REJECTED:
                throw _onGet(new ExecutionException(Messages.format("THEN-000011.promise.rejected"), failure));
            case RESOLVED:
                _onGet(null);
                return value;
            default:
                throw new IllegalStateException(); //TODO Message, should never get here
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
            loop: do {
                switch (this.state) {
                    case CANCELLED:
                    case REJECTED:
                    case RESOLVED:
                        break loop;
                }
                if (!_await(_tryTimeout(end), MILLISECONDS)) {
                    throw new TimeoutException(getTimeoutExceptionMessage());
                }
                _signalAll();
            } while (true);
        } finally {
            _unlock();
        }
        // TODO This read should be fine so long as once state reached terminal state it is never changed
        switch (this.state) {
            case CANCELLED:
                throw _onTimedGet(end, new CancellationException(Messages.format("THEN-000012.promise.cancelled")));
            case REJECTED:
                throw _onTimedGet(end, new ExecutionException(Messages.format("THEN-000011.promise.rejected"), failure));
            case RESOLVED:
                _onTimedGet(end, null);
                return value;
            default:
                throw new IllegalStateException(); //TODO Message, should never get here
        }
    }

    protected <X extends Exception> X _onGet(X exception) throws ExecutionException, InterruptedException {
        for (final Future<?> then : this.<Future<?>>_getEvents(ON_GET)) {
            try {
                then.get();
            } catch (final Throwable e) {
                if (exception != null) {
                    exception.addSuppressed(e);
                }
                // TODO
            }
        }
        return exception;
    }

    protected <X extends Exception> X _onTimedGet(final long end, final X exception) throws TimeoutException, ExecutionException, InterruptedException {
        for (final Future<?> then : this.<Future<?>>_getEvents(ON_GET)) {
            try {
                then.get(_tryTimeout(end), MILLISECONDS);
            } catch (final Throwable e) {
                if (exception != null) {
                    exception.addSuppressed(e);
                }
                // TODO
            }
        }
        return exception;
    }

    protected long _tryTimeout(final long end) throws TimeoutException {
        final long timeout = end - System.currentTimeMillis();
        if (timeout <= 0) {
            throw new TimeoutException(getTimeoutExceptionMessage());
        }
        return timeout;
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
}
