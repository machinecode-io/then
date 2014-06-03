package io.machinecode.then.core.test;

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;
import io.machinecode.then.core.ResolvedPromise;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ResolvedPromiseTest {


    @Test
    public void promiseCompleteTest() throws Exception {
        final Promise<Object> pres = new ResolvedPromise<Object>(null);
        final Count res = new Count();
        Assert.assertEquals(0, res.count);
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        pres.resolve(null);
        Assert.assertEquals(1, res.count);

        final Promise<Object> prej = new ResolvedPromise<Object>(null);
        final Count rej = new Count();
        Assert.assertEquals(0, rej.count);
        prej.onComplete(rej);
        Assert.assertEquals(1, rej.count);
        prej.reject(new Throwable());
        Assert.assertEquals(1, res.count);
    }

    @Test
    public void promiseResolveTest() throws Exception {
        final Object val = new Object();
        final Promise<Object> p = new ResolvedPromise<Object>(val);
        final boolean[] called = new boolean[] { false, false };
        p.reject(new Throwable()); //Should do nothing, other implementations can throw a ResolvedException
        p.resolve(null); //Should also do nothing
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
        });
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseRejectTest() throws Exception {
        final Object val = new Object();
        final Promise<Object> p = new ResolvedPromise<Object>(val);
        final boolean[] called = new boolean[] { false, false };
        p.reject(new Throwable()); //Should do nothing, other implementations can throw a ResolvedException
        p.resolve(null); //Should also do nothing
        p.onComplete(new OnComplete() {
            @Override
            public void complete() {
                called[0] = true;
                if (!p.isResolved()) {
                    Assert.fail("Expected RESOLVED found REJECTED");
                }
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                Assert.fail("Called #onReject for ResolvedPromise");
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                called[1] = true;
                Assert.assertSame("Wrong object provided to #onResolve", val, that);
            }
        });
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseRepeatResolvedTest() throws Exception {
        final Object val = new Object();
        {
            final Promise<Object> p = new ResolvedPromise<Object>(null);
            p.resolve(val);
            p.resolve(val);
        }
        {
            final Promise<Object> p = new ResolvedPromise<Object>(null);
            p.resolve(val);
            p.reject(new Throwable());
        }
    }

    @Test
    public void promiseRepeatRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final Promise<Object> p = new ResolvedPromise<Object>(null);
            p.reject(val);
            p.reject(val);
        }
        {
            final Promise<Object> p = new ResolvedPromise<Object>(null);
            p.reject(val);
            p.resolve(new Object());
        }
    }
}
