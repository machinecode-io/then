package io.machinecode.then.core;

import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 */
public class PromiseImplTest {

    @Test
    public void promiseCompleteTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void> pres = new DeferredImpl<>();
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(0, res.count);
        pres.resolve(null);
        Assert.assertEquals(1, res.count);

        final DeferredImpl<Object,Throwable,Void> prej = new DeferredImpl<>();
        final Count rej = new Count();
        prej.onComplete(rej);
        Assert.assertEquals(0, rej.count);
        prej.reject(new Throwable());
        Assert.assertEquals(1, rej.count);

        final DeferredImpl<Object,Throwable,Void> pcan = new DeferredImpl<>();
        final Count can = new Count();
        pcan.onComplete(can);
        Assert.assertEquals(0, can.count);
        pcan.cancel(true);
        Assert.assertEquals(1, can.count);
    }

    @Test
    public void promiseResolveTest() throws Exception {
        final Object val = new Object();
        final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
        final boolean[] called = new boolean[] { false, false };
        p.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                if (!p.isResolved()) {
                    Assert.fail("Expected RESOLVED found REJECTED");
                }
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                called[1] = true;
                Assert.assertSame("Wrong object provided to #onResolve", val, that);
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                Assert.fail("Called #onReject on #resolve");
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean mayInterrupt) {
                Assert.fail("Called #onCancel on #resolve");
                return false;
            }
        }).resolve(val);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseRejectTest() throws Exception {
        final Throwable val = new Throwable();
        final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
        final boolean[] called = new boolean[] { false, false };
        p.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                if (!p.isRejected()) {
                    Assert.fail("Expected RESOLVED found REJECTED");
                }
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                called[1] = true;
                Assert.assertSame("Wrong exception provided to #onReject", val, fail);
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                Assert.fail("Called #onResolve on #reject");
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean mayInterrupt) {
                Assert.fail("Called #onCancel on #reject");
                return false;
            }
        }).reject(val);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseCancelTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
        final boolean[] called = new boolean[] { false, false };
        p.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                if (!p.isCancelled()) {
                    Assert.fail("Expected CANCELLED found RESOLVED or REJECTED");
                }
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean mayInterrupt) {
                called[1] = true;
                return true;
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                Assert.fail("Called #onResolve on #cancel");
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                Assert.fail("Called #onReject on #cancel");
            }
        }).cancel(true);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseRepeatResolvedTest() throws Exception {
        final Object val = new Object();
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.resolve(val);
            p.resolve(val);
        }
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.resolve(val);
            p.reject(new Throwable());
        }
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.resolve(val);
            p.cancel(true);
        }
    }

    @Test
    public void promiseRepeatRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.reject(val);
            p.reject(val);
        }
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.reject(val);
            p.resolve(new Object());
        }
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.reject(val);
            p.cancel(true);
        }
    }

    @Test
    public void promiseRepeatCancelledTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.cancel(true);
            p.reject(val);
        }
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.cancel(true);
            p.resolve(new Object());
        }
        {
            final DeferredImpl<Object,Throwable,Void> p = new DeferredImpl<>();
            p.cancel(true);
            p.cancel(true);
        }
    }

    @Test
    public void promiseGetTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void> a = new DeferredImpl<>();
        final DeferredImpl<Object,Throwable,Void> b = new DeferredImpl<>();
        final DeferredImpl<Object,Throwable,Void> c = new DeferredImpl<>();

        a.onGet(b);
        b.onGet(c).onCancel(c);

        final boolean[] ret = { false };
        final CountDownLatch l = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    a.get();
                    ret[0] = true;
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    l.countDown();
                }
            }
        }).start();

        a.resolve(new Object());
        Assert.assertTrue(a.isResolved());
        Assert.assertFalse(b.isDone());
        Assert.assertFalse(c.isDone());
        Assert.assertFalse(ret[0]);

        b.cancel(true);
        l.await();
        Assert.assertTrue(a.isResolved());
        Assert.assertTrue(b.isCancelled());
        Assert.assertTrue(c.isCancelled());
        Assert.assertTrue(ret[0]);
    }
}
