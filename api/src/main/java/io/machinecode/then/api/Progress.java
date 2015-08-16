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
package io.machinecode.then.api;

/**
 * Interceptor allowing modification of chained {@link Promise}'s.
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface Progress<T,F,P,Tx,Fx,Px> {

    /**
     * <p>The caller SHOULD ensure that one of {@link Deferred#resolve(Object)},
     * {@link Deferred#reject(Object)} or {@link Deferred#cancel(boolean)} is called on
     * the {@link Deferred} parameter.</p>
     *
     * @param that The resolved value of the original {@link Promise}.
     * @param next A {@link Deferred} that controls the state of the returned promise.
     * @see Promise#then(Progress)
     * @see Deferred#resolve(Object)
     */
    void resolve(final T that, final Deferred<Tx,Fx,Px> next);

    /**
     * <p>The caller SHOULD ensure that one of {@link Deferred#resolve(Object)},
     * {@link Deferred#reject(Object)} or {@link Deferred#cancel(boolean)} is called on
     * the {@link Deferred} parameter.</p>
     *
     * @param that The rejected value of the original {@link Promise}.
     * @param next A {@link Deferred} that controls the state of the returned promise.
     * @see Promise#then(Progress)
     * @see Deferred#reject(Object)
     */
    void reject(final F that, final Deferred<Tx,Fx,Px> next);

    /**
     * <p>Allows modifying the new promises backing {@link Deferred} when the original promise recieves
     * a message via {@link Deferred#progress(Object)}. The caller MAY use this method to call one of
     * {@link Deferred#resolve(Object)}, {@link Deferred#reject(Object)} or {@link Deferred#cancel(boolean)}
     * on the {@link Deferred} parameter.</p>
     *
     * @param that The progress message from the original {@link Promise}
     * @param next A {@link Deferred} that controls the state of the returned promise.
     * @see Promise#then(Progress)
     * @see Deferred#progress(Object)
     */
    void progress(final P that, final Deferred<Tx,Fx,Px> next);
}
