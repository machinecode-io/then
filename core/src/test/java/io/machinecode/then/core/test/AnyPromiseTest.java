package io.machinecode.then.core.test;

import io.machinecode.then.core.AnyPromise;
import io.machinecode.then.core.PromiseImpl;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@SuppressWarnings("unchecked")
public class AnyPromiseTest {

    @Test
    public void promiseResolveTest() throws Exception {
        for (int i = 0; i < 3; ++i) {
            final PromiseImpl<Object,Throwable>[] ares = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> pres = new AnyPromise<Object>(ares);
            final Count res = new Count();
            pres.onComplete(res);
            Assert.assertEquals(0, res.count);
            ares[i].resolve(null);
            Assert.assertEquals(1, res.count);
            Assert.assertTrue(pres.isResolved());
        }
    }

    @Test
    public void promiseRejectTest() throws Exception {
        {
            final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> prej = new AnyPromise<Object>(arej);
            final Count rej = new Count();
            prej.onComplete(rej);
            Assert.assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[1].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[2].reject(new Throwable());
            Assert.assertEquals(1, rej.count);
            Assert.assertTrue(prej.isRejected());
        }
        {
            final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> prej = new AnyPromise<Object>(arej);
            final Count rej = new Count();
            prej.onComplete(rej);
            Assert.assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[1].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[2].resolve(null);
            Assert.assertEquals(1, rej.count);
            Assert.assertTrue(prej.isResolved());
        }
        {
            final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> prej = new AnyPromise<Object>(arej);
            final Count rej = new Count();
            prej.onComplete(rej);
            Assert.assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[2].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[1].resolve(null);
            Assert.assertEquals(1, rej.count);
            Assert.assertTrue(prej.isResolved());
        }
        {
            final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> prej = new AnyPromise<Object>(arej);
            final Count rej = new Count();
            prej.onComplete(rej);
            Assert.assertEquals(0, rej.count);
            arej[1].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[2].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[0].resolve(null);
            Assert.assertEquals(1, rej.count);
            Assert.assertTrue(prej.isResolved());
        }
        {
            final PromiseImpl<Object,Throwable>[] arej = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> prej = new AnyPromise<Object>(arej);
            final Count rej = new Count();
            prej.onComplete(rej);
            Assert.assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            Assert.assertEquals(0, rej.count);
            arej[1].resolve(null);
            Assert.assertEquals(1, rej.count);
            Assert.assertTrue(prej.isResolved());
        }
    }

    @Test
    public void promiseCancelTest() throws Exception {
        {
            final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> pcan = new AnyPromise<Object>(acan);
            final Count can = new Count();
            pcan.onComplete(can);
            Assert.assertEquals(0, can.count);
            acan[0].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[1].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[2].cancel(true);
            Assert.assertEquals(1, can.count);
            Assert.assertTrue(pcan.isRejected());
        }
        {
            final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> pcan = new AnyPromise<Object>(acan);
            final Count can = new Count();
            pcan.onComplete(can);
            Assert.assertEquals(0, can.count);
            acan[0].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[1].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[2].resolve(null);
            Assert.assertEquals(1, can.count);
            Assert.assertTrue(pcan.isResolved());
        }
        {
            final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> pcan = new AnyPromise<Object>(acan);
            final Count can = new Count();
            pcan.onComplete(can);
            Assert.assertEquals(0, can.count);
            acan[0].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[2].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[1].resolve(null);
            Assert.assertEquals(1, can.count);
            Assert.assertTrue(pcan.isResolved());
        }
        {
            final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> pcan = new AnyPromise<Object>(acan);
            final Count can = new Count();
            pcan.onComplete(can);
            Assert.assertEquals(0, can.count);
            acan[1].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[2].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[0].resolve(null);
            Assert.assertEquals(1, can.count);
            Assert.assertTrue(pcan.isResolved());
        }
        {
            final PromiseImpl<Object,Throwable>[] acan = new PromiseImpl[] {
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>(),
                    new PromiseImpl<Object,Throwable>()
            };
            final AnyPromise<Object> pcan = new AnyPromise<Object>(acan);
            final Count can = new Count();
            pcan.onComplete(can);
            Assert.assertEquals(0, can.count);
            acan[0].cancel(true);
            Assert.assertEquals(0, can.count);
            acan[1].resolve(null);
            Assert.assertEquals(1, can.count);
            Assert.assertTrue(pcan.isResolved());
        }
    }
}
