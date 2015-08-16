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

import io.machinecode.then.api.Promise;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class WhenDeferredTest extends Assert {
    
    @Test
    public void arrayResolveTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Throwable,Void> pres = new WhenDeferred<>(ares);
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(0, res.count);
        ares[0].resolve(null);
        assertEquals(0, res.count);
        ares[1].resolve(null);
        assertEquals(0, res.count);
        ares[2].resolve(null);
        assertEquals(1, res.count);
    }

    @Test
    public void arrayRejectTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object, Throwable, Object> prej = new WhenDeferred<>(arej);
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
        final Promise<Object, Throwable, Object> pcan = new WhenDeferred<>(acan);
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
        final Promise<Object,Throwable,Void> pres = new WhenDeferred<>(Arrays.<Promise<?,?,?>>asList(ares));
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(0, res.count);
        ares[0].resolve(null);
        assertEquals(0, res.count);
        ares[1].resolve(null);
        assertEquals(0, res.count);
        ares[2].resolve(null);
        assertEquals(1, res.count);
    }

    @Test
    public void collectionRejectTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object, Throwable, Object> prej = new WhenDeferred<>(Arrays.<Promise<?, ?, ?>>asList(arej));
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
        final Promise<Object, Throwable, Object> pcan = new WhenDeferred<>(Arrays.<Promise<?, ?, ?>>asList(acan));
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
        final Promise<Object,Throwable,Void> pres = new WhenDeferred<>(ares);
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(1, res.count);
        assertTrue(pres.isResolved());
    }

    @Test
    public void emptyArgsCollectionTest() throws Exception {
        final Promise<Object,Throwable,Void> pres = new WhenDeferred<>(Collections.<Promise<?,?,?>>emptyList());
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(1, res.count);
        assertTrue(pres.isResolved());
    }
}
