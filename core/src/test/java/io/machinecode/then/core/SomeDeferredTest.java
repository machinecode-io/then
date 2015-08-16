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

import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class SomeDeferredTest extends Assert {
    
    @Test
    public void arrayResolveTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<List<Object>,Throwable,Void> pres = new SomeDeferred<>(ares);
        final Count<?,?,?> res = new Count<>();
        final Object[] results = new Object[] { new Object(), new Object(), new Object() };
        pres.onComplete(res);
        assertEquals(0, res.count);
        ares[0].resolve(results[0]);
        assertEquals(0, res.count);
        ares[1].resolve(results[1]);
        assertEquals(0, res.count);
        ares[2].resolve(results[2]);
        assertEquals(1, res.count);
        final AtomicBoolean done = new AtomicBoolean(false);
        pres.onResolve(new OnResolve<List<Object>>() {
            @Override
            public void resolve(final List<Object> that) {
                done.set(true);
                assertEquals(results.length, that.size());
                for (int i = 0; i < results.length; ++i) {
                    assertSame(results[i], that.get(i));
                }
            }
        });
        assertTrue(done.get());
    }

    @Test
    public void arrayRejectTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<List<Object>, Throwable, Object> prej = new SomeDeferred<>(arej);
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        assertEquals(1, rej.count);
    }

    @Test
    public void arrayCancelTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<List<Object>, Throwable, Object> pcan = new SomeDeferred<>(acan);
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[0].cancel(true);
        assertEquals(0, can.count);
        acan[1].cancel(true);
        assertEquals(0, can.count);
        acan[2].cancel(true);
        assertEquals(1, can.count);
    }

    @Test
    public void collectionResolveTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<List<Object>,Throwable,Void> pres = new SomeDeferred<>(Arrays.<Promise<Object,Throwable,?>>asList(ares));
        final Count<?,?,?> res = new Count<>();
        final Object[] results = new Object[] { new Object(), new Object(), new Object() };
        pres.onComplete(res);
        assertEquals(0, res.count);
        ares[0].resolve(results[0]);
        assertEquals(0, res.count);
        ares[1].resolve(results[1]);
        assertEquals(0, res.count);
        ares[2].resolve(results[2]);
        assertEquals(1, res.count);
        final AtomicBoolean done = new AtomicBoolean(false);
        pres.onResolve(new OnResolve<List<Object>>() {
            @Override
            public void resolve(final List<Object> that) {
                done.set(true);
                assertEquals(results.length, that.size());
                for (int i = 0; i < results.length; ++i) {
                    assertSame(results[i], that.get(i));
                }
            }
        });
        assertTrue(done.get());
    }

    @Test
    public void collectionRejectTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<List<Object>,Throwable,Object> prej = new SomeDeferred<>(Arrays.<Promise<Object,Throwable,?>>asList(arej));
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        assertEquals(1, rej.count);
    }

    @Test
    public void collectionCancelTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<List<Object>,Throwable,Object> pcan = new SomeDeferred<>(Arrays.<Promise<Object,Throwable,?>>asList(acan));
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[0].cancel(true);
        assertEquals(0, can.count);
        acan[1].cancel(true);
        assertEquals(0, can.count);
        acan[2].cancel(true);
        assertEquals(1, can.count);
    }

    @Test
    public void emptyArgsArrayTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {};
        final Promise<List<Object>,Throwable,Void> pres = new SomeDeferred<>(ares);
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(1, res.count);
        assertTrue(pres.isRejected());
    }

    @Test
    public void emptyArgsCollectionTest() throws Exception {
        final Promise<List<Object>,Throwable,Void> pres = new SomeDeferred<>(Collections.<Promise<Object,Throwable,?>>emptyList());
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(1, res.count);
        assertTrue(pres.isRejected());
    }
}
