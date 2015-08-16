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

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.Promise;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A promise that will be resolved when all the promised passed to it are completed.
 * The outcome of them is ignored.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class WhenDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    final AtomicInteger count = new AtomicInteger(0);

    public WhenDeferred(final Collection<? extends Promise<?,?,?>> promises) {
        if (promises.isEmpty()) {
            resolve(null);
            return;
        }
        final OnComplete complete = new OnComplete() {
            @Override
            public void complete(final int state) {
                if (count.incrementAndGet() == promises.size()) {
                    resolve(null);
                }
            }
        };
        for (final Promise<?,?,?> promise : promises) {
            promise.onComplete(complete);
        }
    }

    public WhenDeferred(final Promise<?,?,?>... promises) {
        this(Arrays.asList(promises));
    }
}
