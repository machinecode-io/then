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

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class CallableDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements Callable<T>, Runnable {

    protected final Callable<? extends T> call;

    public CallableDeferred(final Callable<? extends T> call) {
        this.call = call;
    }

    @Override
    public void run() {
        final T that;
        try {
            that = call.call();
        } catch (final Throwable e) {
            reject(e);
            return;
        }
        resolve(that);
    }

    @Override
    public T call() throws Exception {
        run();
        return get();
    }

    public Runnable asRunnable() {
        return this;
    }

    public Callable<T> asCallable() {
        return this;
    }
}
