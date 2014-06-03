package io.machinecode.then.core;

import io.machinecode.then.api.Chain;
import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnLink;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.Sync;
import org.jboss.logging.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public class AllChain<T> extends BaseChain<T> {

    private static final Logger log = Logger.getLogger(AllChain.class);

    protected final Chain<?>[] link;

    public AllChain(final Chain<?>... link) {
        this.link = link;
        final OnComplete signal = new OnComplete() {
            @Override
            public void complete() {
                signalAll();
            }
        };
        for (final Chain<?> that : link) {
            that.onComplete(signal);
        }
        resolve(null);
    }

    @Override
    public boolean isDone() {
        boolean done = true;
        for (final Deferred<?> that : link) {
            if (that == null) {
                continue;
            }
            done = that.isDone() && done;
        }
        return done;
    }

    @Override
    public AllChain<T> onLink(final OnLink then) {
        if (then == null) {
            throw new IllegalArgumentException(Messages.format("THEN-000100.promise.argument.required", "onLink"));
        }
        RuntimeException exception = null;
        for (final Chain<?> that : link) {
            try {
                then.link(that);
            } catch (final Throwable e) {
                if (exception == null) {
                    exception = new RuntimeException(Messages.format("THEN-000107.deferred.get.exception"), e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        if (exception != null) {
            log().warnf(exception, Messages.format("THEN-000107.deferred.get.exception"));
            throw exception;
        }
        return this;
    }

    @Override
    public void await(final Sync lock) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        RuntimeException exception = null;
        if (link != null) {
            for (final Chain<?> that : link) {
                try {
                    that.await(lock);
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = new RuntimeException(Messages.format("THEN-000107.deferred.get.exception"), e);
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
        }
        if (exception != null) {
            log().warnf(exception, Messages.format("THEN-000107.deferred.get.exception"));
            throw exception;
        }
    }

    @Override
    public void await(final long timeout, final TimeUnit unit, final Sync lock) throws InterruptedException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException(getInterruptedExceptionMessage());
        }
        final long timeoutMillis = unit.toMillis(timeout);
        final long end = System.currentTimeMillis() + timeoutMillis;
        RuntimeException exception = null;
        if (link != null) {
            for (final Chain<?> that : link) {
                try {
                    final long nextTimeout = _tryTimeout(end);
                    that.await(nextTimeout, MILLISECONDS, lock);
                } catch (final TimeoutException e) {
                    throw e;
                } catch (final Throwable e) {
                    if (exception == null) {
                        exception = new RuntimeException(Messages.format("THEN-000107.deferred.get.exception"), e);
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
        }
        if (exception != null) {
            log().warnf(exception, Messages.format("THEN-000107.deferred.get.exception"));
            throw exception;
        }
    }

    @Override
    public AllChain<T> link(final Chain<?> that) {
        // These should always be provided when being constructed
        throw new IllegalStateException(); //TODO Message
    }

    @Override
    protected Logger log() {
        return log;
    }
}
