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
package io.machinecode.then.api;

import java.util.concurrent.Future;

/**
 * <p>A read-only representation of a {@link Deferred} allowing listeners to be notified of state changes, though
 * clients are allowed to attempt to stop the computation using {@link #cancel(boolean)}.</p>
 *
 * @see Deferred
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface Promise<T,F,P> extends OnCancel, Future<T> {

    /**
     * <p>Called to attempt to stop the computation. Calling this method does not guarantee that the computation will
     * cease however if this method is the first terminal method called, an implementation MUST guarantee that any
     * listeners will not by notified of calls to any other terminal methods. After this method has
     * been called {@link #isDone()} will return {@code true}. If this was the first terminal method to be called
     * {@link #isCancelled()} will also return {@code true}.</p>
     *
     * @throws ListenerException MAY be thrown if a listener throws an exception.
     * @param interrupt {@code true} If the computation should be interrupted in the case that it has already commenced.
     * @return {@code true} If the promise was cancelled, {@code false} if it had already reached another terminal state.
     * @see Future#cancel(boolean)
     */
    @Override
    boolean cancel(final boolean interrupt) throws ListenerException;

    /**
     * @return {@code true} if any terminal method has been called.
     */
    @Override
    boolean isDone();

    /**
     * @return {@code true} if {@link Deferred#resolve(Object)} was the first terminal method called.
     */
    boolean isResolved();

    /**
     * @return {@code true} if {@link Deferred#reject(Object)} was  the first terminal method called.
     */
    boolean isRejected();

    /**
     * @return {@code true} if {@link #cancel(boolean)} was the first terminal method called.
     */
    @Override
    boolean isCancelled();

    /**
     * <p>Triggered when {@link Deferred#resolve(Object)} is the first terminal method called.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onResolve(final OnResolve<? super T> then);

    /**
     * <p>Triggered when {@link Deferred#reject(Object)} is the first terminal method called.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onReject(final OnReject<? super F> then);

    /**
     * <p>Triggered when {@link #cancel(boolean)} is the first terminal method called.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onCancel(final OnCancel then);

    /**
     * <p>Triggered on any event after which {@link #isDone()} will return {@code true}.</p>
     *
     * <p>Will be fired in addition to the callback for the specific event.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onComplete(final OnComplete then);

    /**
     * <p>Triggered when {@link Deferred#progress(Object)} is called.</p>
     *
     * <p>An implementation MUST ensure that the value from a call to {@link Deferred#progress(Object)} is reported to
     * every listener that is registered prior to that call. Values from calls to {@link Deferred#progress(Object)}
     * before this method was called MAY be sent to the listener. If they are they MUST be sent in the same order
     * they were received by {@link Deferred#progress(Object)};</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onProgress(final OnProgress<? super P> then);

    /**
     * <p>Triggered when {@link #get(long, java.util.concurrent.TimeUnit)} or {@link #get()} is called.
     * It will be called after this promise has transitioned into a state
     * where {@link #isDone()} will return {@code true}.</p>
     *
     * <p>Each get method will call the corresponding get method on the {@link Future}
     * in the thread that called either {@link #get()} or {@link #get(long, java.util.concurrent.TimeUnit)}.</p>
     *
     * @param then Callback to be executed
     * @return This instance for method chaining.
     */
    Promise<T,F,P> onGet(final Future<?> then);

    /**
     * <p>Return a new promise that will have the resolved value of the original {@link Promise} converted to
     * the new type using the {@link Resolve} parameter.</p>
     *
     * <p>The new promise MUST end in the same state as the {@link Promise} that created it if the
     * {@link Deferred} reaches a terminal state through either of {@link Deferred#reject(Object)} or
     * {@link Deferred#cancel(boolean)}. If {@link Deferred#resolve(Object)} is called then this promise
     * may reach any terminal state from a call to the {@link Deferred} provided to {@link Resolve#resolve(Object, Deferred)}.</p>
     *
     * @param then A processor to convert the resolved value from type {@code T} to type {@code Tx}.
     * @param <Tx> Type of the new promise {@link Deferred#resolve(Object)}.
     * @return A new promise with resolved type {@code Tx}.
     * @see Resolve
     */
    <Tx> Promise<Tx,F,P> then(final Resolve<? super T,Tx,F,P> then);

    /**
     * <p>Return a new promise that will have the resolved value of the original {@link Promise} converted to
     * the new type using {@literal then}.</p>
     *
     * <p>The new promise MUST end in the same state as the {@link Promise} that created it if the
     * {@link Deferred} reaches a terminal state through {@link Deferred#cancel(boolean)}. If
     * {@link Deferred#resolve(Object)} or {@link Deferred#reject(Object)} is called then this promise
     * may reach any terminal state from a call to the {@link Deferred} provided to either
     * {@link Reject#resolve(Object, Deferred)} or {@link Reject#reject(Object, Deferred)} respectively.</p>
     *
     * @param then A processor to convert a resolved value from type {@code T} to type {@code Tx} or
     *             rejected value from {@code F} to {@code Fx}.
     * @param <Tx> Type of the new promise {@link Deferred#resolve(Object)}.
     * @param <Fx> Type of the new promise {@link Deferred#reject(Object)}.
     * @return A new promise with resolved type {@code Tx} and rejected type {@code Fx}.
     * @see Reject
     */
    <Tx,Fx> Promise<Tx,Fx,P> then(final Reject<? super T,? super F,Tx,Fx,P> then);

    /**
     * <p>Return a new promise that will have the resolved value of the original {@link Promise} converted to
     * the new type using {@code then}.</p>
     *
     * <p>The new promise MUST end in the same state as the {@link Promise} that created it if the
     * {@link Deferred} reaches a terminal state through {@link Deferred#cancel(boolean)}. If
     * {@link Deferred#resolve(Object)} or {@link Deferred#reject(Object)} is called then this promise
     * SHOULD reach a terminal state from a call to the {@link Deferred} provided to either
     * {@link Progress#resolve(Object, Deferred)} or {@link Progress#reject(Object, Deferred)} respectively.
     * It MAY reach a terminal state through {@link Progress#progress(Object, Deferred)}.</p>
     *
     * @param then A processor to convert a resolved value from type {@code T} to type {@code Tx}, rejected
     *             value from {@code F} to {@code Fx} or progress value from {@code P} to {@code Px}.
     * @param <Tx> Type of the new promise {@link Deferred#resolve(Object)}.
     * @param <Fx> Type of the new promise {@link Deferred#reject(Object)}.
     * @param <Px> Type of the new promise {@link Deferred#progress(Object)}.
     * @return A new promise with resolved type {@code Tx}, rejected type {@code Fx} and progress type {@code Px}.
     * @see Progress
     */
    <Tx,Fx,Px> Promise<Tx,Fx,Px> then(final Progress<? super T,? super F,? super P,Tx,Fx,Px> then);
}
