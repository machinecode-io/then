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

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class OnceDeferredTest {

    //Test OnceDeferred which should throw various CompletionException if completion is attempted twice

    @Test
    public void deferredAlreadyResolvedTest() throws Exception {
        final Object val = new Object();
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.resolve(val);
            p.resolve(val);
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }

        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.resolve(val);
            p.reject(new Throwable());
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
        final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
        p.resolve(val);
        p.cancel(true);
    }

    @Test
    public void deferredAlreadyRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.reject(val);
            p.reject(val);
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.reject(val);
            p.resolve(new Object());
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
        p.reject(val);
        p.cancel(true);
    }

    @Test
    public void deferredAlreadyCancelledTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.cancel(true);
            p.reject(val);
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.cancel(true);
            p.resolve(new Object());
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
        p.cancel(true);
        p.cancel(true);
        // Cancel should be allowed to work with Future
    }
}
