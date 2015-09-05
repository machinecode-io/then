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
public class RunnableDeferred<T,P> extends DeferredImpl<T,Throwable,P> implements Callable<T>, Runnable {

    protected final Runnable call;

    public RunnableDeferred(final Runnable call, final T value) {
        this.call = call;
        //This is safe, as this.value should never be read unless this.state == RESOLVED which requires calling #resolve
        this.value = value;
    }

    @Override
    public void run() {
        try {
            call.run();
        } catch (final Throwable e) {
            reject(e);
            return;
        }
        resolve(value);
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
