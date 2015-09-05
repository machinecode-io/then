/*
 * Copyright 2015 Brent Douglas and other contributors
 * as indicated by the @author tags. All rights reserved.
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

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A promise that will be resolved with the values of each of the resolved promises
 * passed in once all the promises are complete. If none complete it will be rejected.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class SomeDeferred<T,F,P> extends DeferredImpl<List<T>,F,P> {

    public SomeDeferred(final Collection<? extends Promise<T,?,?>> promises) {
        if (promises.isEmpty()) {
            reject(null);
            return;
        }
        final Callback callback = new Callback(promises.size());
        for (final Promise<T,?,?> promise : promises) {
            promise.onResolve(callback)
                    .onComplete(callback);
        }
    }

    public SomeDeferred(final Promise<T,?,?>... promises) {
        this(Arrays.asList(promises));
    }

    private class Callback implements OnResolve<T>, OnComplete {
        final AtomicInteger count = new AtomicInteger(0);
        final int size;
        final List<T> ret;

        private Callback(final int size) {
            this.size = size;
            this.ret = new ArrayList<>(size);
        }

        @Override
        public void resolve(final T that) {
            ret.add(that);
        }

        @Override
        public void complete(final int state) {
            if (count.incrementAndGet() == size) {
                if (ret.isEmpty()) {
                    reject(null);
                } else {
                    SomeDeferred.this.resolve(ret);
                }
            }
        }
    }
}
