package io.machinecode.then.core;

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;

/**
 * <p>A {@link io.machinecode.then.api.Deferred} implementation that will throw a {@link io.machinecode.then.api.CompletionException}
 * if completion is attempted multiple times.</p>
 *
 * <p>{@link #cancel(boolean)} will never throw a completion exception in order to maintain compatibility
 * with {@link java.util.concurrent.Future#cancel(boolean)}</p>
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class OnceDeferred<T,F extends Throwable,P> extends DeferredImpl<T,F,P> {

    @Override
    protected boolean setValue(final T value) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000007.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000006.promise.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000008.promise.already.cancelled"));
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
                throw new RejectedException(Messages.get("THEN-000007.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000006.promise.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000008.promise.already.cancelled"));
            default:
                this.failure = failure;
                this.state = REJECTED;
        }
        return false;
    }
}
