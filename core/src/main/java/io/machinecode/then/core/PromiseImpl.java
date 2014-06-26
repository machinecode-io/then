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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class PromiseImpl<T,F extends Throwable> implements Promise<T,F> {

    private static final Logger log = Logger.getLogger(PromiseImpl.class);

    protected static final byte ON_RESOLVE     = 100;
    protected static final byte ON_REJECT      = 101;
    protected static final byte ON_CANCEL      = 102;
    protected static final byte ON_COMPLETE    = 103;
    protected static final byte ON_GET         = 104;

    protected volatile byte state = PENDING;

    protected volatile T value;
    protected volatile F failure;

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
        if (_pos >= _events.length) {
            final Events[] events = new Events[_events.length + 1];
            System.arraycopy(_events, 0, events, 0, _pos);
            _events = events;
        }
        _events[_pos++] = new Events(event, that);
    }

    protected <X> List<X> _getEvents(final byte event) {
        final List<X> list = new LinkedList<X>();
        for (int i = 0; i < _pos; ++i) {
            if (event == _events[i].event) {
                list.add((X) _events[i].value);
            }
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

    protected boolean setFailure(final F failure) {
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
        final List<OnResolve<T>> onResolves;
        final List<OnComplete> onCompletes;
        _lock();
        try {
            if (setValue(value)) {
                return;
            }
            onResolves = this._getEvents(ON_RESOLVE);
            onCompletes = this._getEvents(ON_COMPLETE);
        } finally {
            _unlock();
        }
        Throwable exception = null;
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
    public void reject(final F failure) {
        log().tracef(failure, getRejectLogMessage());
        final List<OnReject<F>> onRejects;
        final List<OnComplete> onCompletes;
        _lock();
        try {
            if (setFailure(failure)) {
                return;
            }
            onRejects = this._getEvents(ON_REJECT);
            onCompletes = this._getEvents(ON_COMPLETE);
        } finally {
            _unlock();
        }
        for (final OnReject<F> then : onRejects) {
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
        final List<OnCancel> onCancels;
        final List<OnComplete> onCompletes;
        _lock();
        try {
            if (setCancelled()) {
                return isCancelled();
            }
            onCancels = this._getEvents(ON_CANCEL);
            onCompletes = this._getEvents(ON_COMPLETE);
        } finally {
            _unlock();
        }
        for (final OnCancel then : onCancels) {
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
        for (final OnComplete on : onCompletes) {
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
    public PromiseImpl<T,F> onResolve(final OnResolve<T> then) {
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
    public PromiseImpl<T,F> onReject(final OnReject<F> then) {
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
    public PromiseImpl<T,F> onCancel(final OnCancel then) {
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
    public PromiseImpl<T,F> onComplete(final OnComplete then) {
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
    public Promise<T,F> onGet(final Future<?> then) {
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
        final List<Future<?>> onGets;
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
            onGets = this._getEvents(ON_GET);
        } finally {
            _unlock();
        }
        // TODO This read should be fine so long as once state reaches a terminal state it is never changed
        switch (this.state) {
            case CANCELLED:
                throw _onGet(onGets, new CancellationException(Messages.format("THEN-000012.promise.cancelled")));
            case REJECTED:
                throw _onGet(onGets, new ExecutionException(Messages.format("THEN-000011.promise.rejected"), failure));
            case RESOLVED:
                _onGet(onGets, null);
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
        final List<Future<?>> onGets;
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
            onGets = this._getEvents(ON_GET);
        } finally {
            _unlock();
        }
        // TODO This read should be fine so long as once state reaches a terminal state it is never changed
        switch (this.state) {
            case CANCELLED:
                throw _onTimedGet(onGets, end, new CancellationException(Messages.format("THEN-000012.promise.cancelled")));
            case REJECTED:
                throw _onTimedGet(onGets, end, new ExecutionException(Messages.format("THEN-000011.promise.rejected"), failure));
            case RESOLVED:
                _onTimedGet(onGets, end, null);
                return value;
            default:
                throw new IllegalStateException(); //TODO Message, should never get here
        }
    }

    protected <X extends Exception> X _onGet(final List<Future<?>> onGets, X exception) throws ExecutionException, InterruptedException {
        for (final Future<?> then : onGets) {
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

    protected <X extends Exception> X _onTimedGet(final List<Future<?>> onGets, final long end, final X exception) throws TimeoutException, ExecutionException, InterruptedException {
        for (final Future<?> then : onGets) {
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
