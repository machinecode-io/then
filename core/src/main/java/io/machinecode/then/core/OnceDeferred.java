package io.machinecode.then.core;

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.CompletedException;
import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;

/**
 * A {@link Deferred} implementation that will throw a {@link CompletedException} completion is attempted multiple times.
 *
 * {@link #cancel()} and {@link #cancel(boolean)} will never throw a completion exception in
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class OnceDeferred<T> extends DeferredImpl<T> {

    @Override
    protected boolean setValue(final T value) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000109.deferred.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000108.deferred.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000110.deferred.already.cancelled"));
            default:
                this.value = value;
                this.state = RESOLVED;
        }
        return false;
    }

    @Override
    protected boolean setFailure(final Throwable failure) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000109.deferred.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000108.deferred.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000110.deferred.already.cancelled"));
            default:
                this.failure = failure;
                this.state = REJECTED;
        }
        return false;
    }

    @Override
    protected boolean checkCancelled(final boolean futureCompatible) {
        if (futureCompatible) {
            return this.isDone();
        }
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000109.deferred.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000108.deferred.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000110.deferred.already.cancelled"));
        }
        return false;
    }
}
