package io.machinecode.then.core;

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;

/**
 * A {@link Promise} implementation that will throw a {@link io.machinecode.then.api.CompletionException}
 * if completion is attempted multiple times.
 *
 * {@link #cancel(boolean)} will never throw a completion exception in order to maintain compatibility
 * with {@link java.util.concurrent.Future#cancel(boolean)}
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class OncePromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    @Override
    protected boolean setValue(final T value) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000006.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000005.promise.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000010.promise.already.cancelled"));
            default:
                this.value = value;
                this.state = RESOLVED;
        }
        return false;
    }

    @Override
    protected boolean setFailure(final F failure) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000006.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000005.promise.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000010.promise.already.cancelled"));
            default:
                this.failure = failure;
                this.state = REJECTED;
        }
        return false;
    }
}
