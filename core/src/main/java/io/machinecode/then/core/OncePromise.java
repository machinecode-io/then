package io.machinecode.then.core;

import io.machinecode.then.api.CompletedException;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;

/**
 * A {@link Promise} implementation that will throw a {@link CompletedException} completion is attempted multiple times.
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class OncePromise<T> extends PromiseImpl<T> {

    @Override
    protected boolean setValue(final T value) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000006.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000005.promise.already.resolved"));
            default:
                this.state = RESOLVED;
                this.value = value;
        }
        return false;
    }

    @Override
    protected boolean setFailure(final Throwable failure) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000006.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000005.promise.already.resolved"));
            default:
                this.failure = failure;
                this.state = REJECTED;
        }
        return false;
    }
}
