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

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class CancelledDeferredTest {

    @Test
    public void promiseCompleteTest() throws Exception {
        final Deferred<Object,Throwable,Void> pres = new CancelledDeferred<>(true);
        final Count<?,?,?> res = new Count<>();
        Assert.assertEquals(0, res.count);
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        pres.resolve(null);
        Assert.assertEquals(1, res.count);

        final Deferred<Object,Throwable,Void> prej = new CancelledDeferred<>(true);
        final Count<?,?,?> rej = new Count<>();
        Assert.assertEquals(0, rej.count);
        prej.onComplete(rej);
        Assert.assertEquals(1, rej.count);
        prej.reject(new Throwable());
        Assert.assertEquals(1, res.count);
    }

    @Test
    public void promiseRejectTest() throws Exception {
        final Deferred<Object,Throwable,Void> p = new CancelledDeferred<>(true);
        final boolean[] called = new boolean[] { false, false };
        p.reject(new Throwable()); //Should do nothing, other implementations can throw a ResolvedException
        p.resolve(null); //Should also do nothing
        p.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                if (!p.isCancelled()) {
                    Assert.fail("Expected CANCELLED found " + p.toString());
                }
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                Assert.fail("Called #onReject for CancelledDeferred");
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                Assert.fail("Called #onResolve for CancelledDeferred");
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean mayInterrupt) {
                called[1] = true;
                return true;
            }
        });
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseRepeatResolvedTest() throws Exception {
        final Object val = new Object();
        {
            final Deferred<Object,Throwable,Void> p = new CancelledDeferred<>(true);
            p.resolve(val);
            p.resolve(val);
        }
        {
            final Deferred<Object,Throwable,Void> p = new CancelledDeferred<>(true);
            p.resolve(val);
            p.reject(new Throwable());
        }
    }

    @Test
    public void promiseRepeatRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final Deferred<Object,Throwable,Void> p = new CancelledDeferred<>(true);
            p.reject(val);
            p.reject(val);
        }
        {
            final Deferred<Object,Throwable,Void> p = new CancelledDeferred<>(true);
            p.reject(val);
            p.resolve(new Object());
        }
    }
}
