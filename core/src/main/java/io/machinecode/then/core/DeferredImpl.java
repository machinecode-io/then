package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.FailureException;
import io.machinecode.then.api.ListenerException;
import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnProgress;
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
 * <p>A thread safe {@link Deferred} implementation that silently drops multiple calls to terminal methods.</p>
 *
 * It will not report progress to a listener if the listener is added after the call to {@link #progress(Object)}
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class DeferredImpl<T,F,P> implements Deferred<T,F,P> {

    private static final Logger log = Logger.getLogger(DeferredImpl.class);

    protected static final byte ON_RESOLVE     = 100;
    protected static final byte ON_REJECT      = 101;
    protected static final byte ON_CANCEL      = 102;
    protected static final byte ON_COMPLETE    = 103;
    protected static final byte ON_PROGRESS    = 104;
    protected static final byte ON_GET         = 105;

    protected volatile byte state = PENDING;

    protected T value;
    protected F failure;

    private final ReentrantLock _lock = new ReentrantLock();
    private final Condition _condition = _lock.newCondition();

    private Event[] _events;
    private int _pos = 0;

    private static class Event {
        final byte event;
        final Object value;

        private Event(final byte event, final Object value) {
            this.event = event;
            this.value = value;
        }
    }

    protected void _addEvent(final byte event, final Object that) {
        if (_pos >= _events.length) {
            final Event[] events = new Event[_events.length + 1];
            System.arraycopy(_events, 0, events, 0, _pos);
            _events = events;
        }
        _events[_pos++] = new Event(event, that);
    }

    @SuppressWarnings("unchecked")
    protected <X> List<X> _getEvents(final byte event) {
        final List<X> list = new LinkedList<>();
        for (int i = 0; i < _pos; ++i) {
            if (event == _events[i].event) {
                list.add((X) _events[i].value);
            }
        }
        return list;
    }

    public DeferredImpl() {
        this(2);
    }

    public DeferredImpl(final int hint) {
        this._events = new Event[hint];
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
    public Promise<T,F,P> promise() {
        return this;
    }

    @Override
    public void resolve(final T value) throws ListenerException {
        log().tracef(getResolveLogMessage(), value);
        final List<OnResolve<T>> onResolves;
        final List<OnComplete> onCompletes;
        final int state;
        _lock();
        try {
            if (setValue(value)) {
                return;
            }
            onResolves = this._getEvents(ON_RESOLVE);
            onCompletes = this._getEvents(ON_COMPLETE);
            state = this.state;
        } finally {
            _unlock();
        }
        ListenerException exception = null;
        for (final OnResolve<T> on : onResolves) {
            try {
                on.resolve(this.value);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000300.promise.on.resolve.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        for (final OnComplete on : onCompletes) {
            try {
                on.complete(state);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000303.promise.on.complete.exception"), e);
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
    }

    @Override
    public void reject(final F failure) {
        log().tracef(getRejectLogMessage(), failure);
        final List<OnReject<F>> onRejects;
        final List<OnComplete> onCompletes;
        final int state;
        _lock();
        try {
            if (setFailure(failure)) {
                return;
            }
            onRejects = this._getEvents(ON_REJECT);
            onCompletes = this._getEvents(ON_COMPLETE);
            state = this.state;
        } finally {
            _unlock();
        }
        ListenerException exception = null;
        for (final OnReject<F> then : onRejects) {
            try {
                then.reject(this.failure);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000301.promise.on.reject.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        for (final OnComplete on : onCompletes) {
            try {
                on.complete(state);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000303.promise.on.complete.exception"), e);
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
    }

    @Override
    public void progress(final P that) {
        log().tracef(getProgressLogMessage(), that);
        final List<OnProgress<P>> onProgresses;
        _lock();
        try {
            onProgresses = this._getEvents(ON_PROGRESS);
        } finally {
            _unlock();
        }
        ListenerException exception = null;
        for (final OnProgress<P> then : onProgresses) {
            try {
                then.progress(that);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000304.promise.on.progress.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) throws ListenerException {
        log().tracef(getCancelLogMessage());
        final List<OnCancel> onCancels;
        final List<OnComplete> onCompletes;
        final int state;
        _lock();
        try {
            if (setCancelled()) {
                return isCancelled();
            }
            onCancels = this._getEvents(ON_CANCEL);
            onCompletes = this._getEvents(ON_COMPLETE);
            state = this.state;
        } finally {
            _unlock();
        }
        ListenerException exception = null;
        for (final OnCancel then : onCancels) {
            try {
                then.cancel(mayInterruptIfRunning);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000302.promise.on.cancel.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        for (final OnComplete on : onCompletes) {
            try {
                on.complete(state);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new ListenerException(Messages.format("THEN-000303.promise.on.complete.exception"), e);
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
    public Deferred<T,F,P> onResolve(final OnResolve<T> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000400.promise.argument.required", "onResolve"));
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
    public Deferred<T,F,P> onReject(final OnReject<F> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000400.promise.argument.required", "onReject"));
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
    public Deferred<T,F,P> onCancel(final OnCancel then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000400.promise.argument.required", "onCancel"));
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
    public Deferred<T,F,P> onComplete(final OnComplete then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000400.promise.argument.required", "onComplete"));
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
            then.complete(this.state);
        }
        return this;
    }

    @Override
    public Deferred<T, F, P> onProgress(final OnProgress<P> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000400.promise.argument.required", "onProgress"));
        }
        _lock();
        try {
            _addEvent(ON_PROGRESS, then);
        } finally {
            _unlock();
        }
        return this;
    }

    @Override
    public Deferred<T,F,P> onGet(final Future<?> then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000400.promise.argument.required", "onGet"));
        }
        _lock();
        try {
            _addEvent(ON_GET, then);
        } finally {
            _unlock();
        }
        return this;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return _get();
    }

    protected T _get() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final List<Future<?>> onGets;
        final int state;
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
            state = this.state;
            onGets = this._getEvents(ON_GET);
        } finally {
            _unlock();
        }
        switch (state) {
            case CANCELLED:
                throw _onGet(onGets, new CancellationException(Messages.format("THEN-000202.promise.cancelled")));
            case REJECTED:
                final String msg = Messages.format("THEN-000201.promise.rejected");
                throw _onGet(onGets, new ExecutionException(msg, _getFailureCause(msg)));
            case RESOLVED:
                _onGet(onGets, null);
                return value;
            default:
                throw new IllegalStateException(Messages.format("THEN-000200.promise.illegal.state", _stateToString(state)));
        }
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return _get(timeout, unit);
    }

    protected T _get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final List<Future<?>> onGets;
        final long end = System.currentTimeMillis() + unit.toMillis(timeout);
        final byte state;
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
            state = this.state;
            onGets = this._getEvents(ON_GET);
        } finally {
            _unlock();
        }
        switch (state) {
            case CANCELLED:
                throw _onTimedGet(onGets, end, new CancellationException(Messages.format("THEN-000202.promise.cancelled")));
            case REJECTED:
                final String msg = Messages.format("THEN-000201.promise.rejected");
                throw _onTimedGet(onGets, end, new ExecutionException(msg, _getFailureCause(msg)));
            case RESOLVED:
                _onTimedGet(onGets, end, null);
                return value;
            default:
                throw new IllegalStateException(Messages.format("THEN-000200.promise.illegal.state", _stateToString(state)));
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
                log.tracef(e, Messages.get("THEN-000401.promise.get.exception"));
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
                log.tracef(e, Messages.get("THEN-000401.promise.get.exception"));
            }
        }
        return exception;
    }

    protected Throwable _getFailureCause(final String msg) {
        return failure instanceof Throwable
                ? (Throwable)failure
                : new FailureException(msg, failure);
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

    protected String getProgressLogMessage() {
        return Messages.get("THEN-000003.promise.progress");
    }

    protected String getTimeoutExceptionMessage() {
        return Messages.get("THEN-000100.promise.timeout");
    }

    protected String getInterruptedExceptionMessage() {
        return Messages.format("THEN-000101.promise.interrupted");
    }

    protected Logger log() {
        return log;
    }

    protected String _stateToString(final int state) {
        switch (state) {
            case PENDING: return "PENDING";
            case RESOLVED: return "RESOLVED";
            case REJECTED: return "REJECTED";
            case CANCELLED: return "CANCELLED";
            default: return "UNKNOWN";
        }
    }
}
