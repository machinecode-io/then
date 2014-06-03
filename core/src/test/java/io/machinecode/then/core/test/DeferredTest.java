package io.machinecode.then.core.test;

import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.core.DeferredImpl;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class DeferredTest {

    @Test
    public void deferredCompleteTest() throws Exception {
        final DeferredImpl<Object> pres = new DeferredImpl<Object>();
        final Count res = new Count();
        pres.onComplete(res);
        Assert.assertEquals(0, res.count);
        pres.resolve(null);
        Assert.assertEquals(1, res.count);

        final DeferredImpl<Object> prej = new DeferredImpl<Object>();
        final Count rej = new Count();
        prej.onComplete(rej);
        Assert.assertEquals(0, rej.count);
        prej.reject(new Throwable());
        Assert.assertEquals(1, rej.count);

        final DeferredImpl<Object> pcan = new DeferredImpl<Object>();
        final Count can = new Count();
        pcan.onComplete(can);
        Assert.assertEquals(0, can.count);
        pcan.cancel();
        Assert.assertEquals(1, can.count);
    }

    @Test
    public void deferredResolveTest() throws Exception {
        final Object val = new Object();
        final DeferredImpl<Object> p = new DeferredImpl<Object>();
        final boolean[] called = new boolean[] { false, false };
        p.onComplete(new OnComplete() {
            @Override
            public void complete() {
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
            public void cancel() {
                Assert.fail("Called #onCancel on #resolve");
            }
        }).resolve(val);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void deferredRejectTest() throws Exception {
        final Throwable val = new Throwable();
        final DeferredImpl<Object> p = new DeferredImpl<Object>();
        final boolean[] called = new boolean[] { false, false };
        p.onComplete(new OnComplete() {
            @Override
            public void complete() {
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
            public void cancel() {
                Assert.fail("Called #onCancel on #reject");
            }
        }).reject(val);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void deferredCancelTest() throws Exception {
        final DeferredImpl<Object> p = new DeferredImpl<Object>();
        final boolean[] called = new boolean[] { false, false };
        p.onComplete(new OnComplete() {
            @Override
            public void complete() {
                called[0] = true;
                if (!p.isCancelled()) {
                    Assert.fail("Expected CANCELLED found RESOLVED or REJECTED");
                }
            }
        }).onCancel(new OnCancel() {
            @Override
            public void cancel() {
                called[1] = true;
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
        }).cancel();
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void deferredRepeatResolvedTest() throws Exception {
        final Object val = new Object();
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.resolve(val);
            p.resolve(val);
        }
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.resolve(val);
            p.reject(new Throwable());
        }
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.resolve(val);
            p.cancel();
        }
    }

    @Test
    public void deferredRepeatRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.reject(val);
            p.reject(val);
        }
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.reject(val);
            p.resolve(new Object());
        }
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.reject(val);
            p.cancel();
        }
    }

    @Test
    public void deferredRepeatCancelledTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.cancel();
            p.reject(val);
        }
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.cancel();
            p.resolve(new Object());
        }
        {
            final DeferredImpl<Object> p = new DeferredImpl<Object>();
            p.cancel();
            p.cancel();
        }
    }
}
