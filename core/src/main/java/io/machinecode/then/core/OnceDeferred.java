/*
 * Copyright 2015 Brent Douglas and other contributors
 * as indicated by the @authors tag. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class OnceDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    @Override
    protected boolean setValue(final T value) {
        switch (this.state) {
            case REJECTED:
                throw new RejectedException(Messages.get("THEN-000103.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000102.promise.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000104.promise.already.cancelled"));
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
                throw new RejectedException(Messages.get("THEN-000103.promise.already.rejected"));
            case RESOLVED:
                throw new ResolvedException(Messages.get("THEN-000102.promise.already.resolved"));
            case CANCELLED:
                throw new CancelledException(Messages.get("THEN-000104.promise.already.cancelled"));
            default:
                this.failure = failure;
                this.state = REJECTED;
        }
        return false;
    }
}
