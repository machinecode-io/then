package io.machinecode.then.core;

import io.machinecode.then.api.Promise;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 */
@SuppressWarnings("unchecked")
public class AllDeferredTest {

    @Test
    public void arrayResolveTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Throwable,Void> pres = new AllDeferred<Object,Throwable,Void>(ares);
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(0, res.count);
        ares[0].resolve(null);
        Assert.assertEquals(0, res.count);
        ares[1].resolve(null);
        Assert.assertEquals(0, res.count);
        ares[2].resolve(null);
        Assert.assertEquals(1, res.count);
    }

    @Test
    public void arrayRejectTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object, Throwable, Object> prej = Then.all(arej);
        final Count rej = new Count();
        prej.onComplete(rej);
        Assert.assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        Assert.assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        Assert.assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        Assert.assertEquals(1, rej.count);
    }

    @Test
    public void arrayCancelTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object, Throwable, Object> pcan = Then.all(acan);
        final Count can = new Count();
        pcan.onComplete(can);
        Assert.assertEquals(0, can.count);
        acan[0].cancel(true);
        Assert.assertEquals(0, can.count);
        acan[1].cancel(true);
        Assert.assertEquals(0, can.count);
        acan[2].cancel(true);
        Assert.assertEquals(1, can.count);
    }

    @Test
    public void collectionResolveTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Throwable,Void> pres = new AllDeferred<Object,Throwable,Void>(Arrays.<Promise<?,?,?>>asList(ares));
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(0, res.count);
        ares[0].resolve(null);
        Assert.assertEquals(0, res.count);
        ares[1].resolve(null);
        Assert.assertEquals(0, res.count);
        ares[2].resolve(null);
        Assert.assertEquals(1, res.count);
    }

    @Test
    public void collectionRejectTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object, Throwable, Object> prej = Then.all(Arrays.<Promise<?,?,?>>asList(arej));
        final Count rej = new Count();
        prej.onComplete(rej);
        Assert.assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        Assert.assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        Assert.assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        Assert.assertEquals(1, rej.count);
    }

    @Test
    public void collectionCancelTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object, Throwable, Object> pcan = Then.all(Arrays.<Promise<?,?,?>>asList(acan));
        final Count can = new Count();
        pcan.onComplete(can);
        Assert.assertEquals(0, can.count);
        acan[0].cancel(true);
        Assert.assertEquals(0, can.count);
        acan[1].cancel(true);
        Assert.assertEquals(0, can.count);
        acan[2].cancel(true);
        Assert.assertEquals(1, can.count);
    }

    @Test
    public void emptyArgsArrayTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {};
        final Promise<Object,Throwable,Void> pres = new AllDeferred<Object,Throwable,Void>(ares);
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        Assert.assertTrue(pres.isResolved());
    }

    @Test
    public void emptyArgsCollectionTest() throws Exception {
        final Promise<Object,Throwable,Void> pres = new AllDeferred<Object,Throwable,Void>(Collections.<Promise<?,?,?>>emptyList());
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        Assert.assertTrue(pres.isResolved());
    }
}
