package io.machinecode.then.core.test;

import io.machinecode.then.api.Promise;
import io.machinecode.then.core.AllPromise;
import io.machinecode.then.core.PromiseImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@SuppressWarnings("unchecked")
public class AllPromiseTest {

    @Test
    public void arrayResolveTest() throws Exception {
        final PromiseImpl<Object,Throwable>[] ares = new PromiseImpl[] {
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>()
        };
        final AllPromise<Object,Throwable> pres = new AllPromise<Object,Throwable>(ares);
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
        final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>()
        };
        final AllPromise<Object,Throwable> prej = new AllPromise<Object,Throwable>(arej);
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
        final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>()
        };
        final AllPromise<Object,Throwable> pcan = new AllPromise<Object,Throwable>(acan);
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
        final PromiseImpl<Object,Throwable>[] ares = new PromiseImpl[] {
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>()
        };
        final AllPromise<Object,Throwable> pres = new AllPromise<Object,Throwable>(Arrays.<Promise<?, ?>>asList(ares));
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
        final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>()
        };
        final AllPromise<Object,Throwable> prej = new AllPromise<Object,Throwable>(Arrays.<Promise<?, ?>>asList(arej));
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
        final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>(),
                new PromiseImpl<Object,Throwable>()
        };
        final AllPromise<Object,Throwable> pcan = new AllPromise<Object,Throwable>(Arrays.<Promise<?, ?>>asList(acan));
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
        final PromiseImpl<Object,Throwable>[] ares = new PromiseImpl[] {};
        final AllPromise<Object,Throwable> pres = new AllPromise<Object,Throwable>(ares);
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        Assert.assertTrue(pres.isResolved());
    }

    @Test
    public void emptyArgsCollectionTest() throws Exception {
        final AllPromise<Object,Throwable> pres = new AllPromise<Object,Throwable>(Collections.<Promise<?, ?>>emptyList());
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        Assert.assertTrue(pres.isResolved());
    }
}
