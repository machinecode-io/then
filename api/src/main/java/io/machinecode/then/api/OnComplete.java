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
 * <p>Listener for a {@link Deferred} entering a state where {@link Deferred#isDone()}
 * returns {@code true}.</p>
 *
 * @see Deferred
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public interface OnComplete {

    /**
     * @param state The final state of the {@link Deferred}. It is not guaranteed that this parameter will be
     *              one of the constants defined in {@link Promise}, inheritors MAY provide alternate
     *              terminal states.
     *
     * @see Deferred#RESOLVED
     * @see Deferred#REJECTED
     * @see Deferred#PENDING
     */
    void complete(final int state);
}
