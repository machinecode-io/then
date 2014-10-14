package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class ResolvedPromiseTest {


    @Test
    public void promiseCompleteTest() throws Exception {
        final Deferred<Object,Throwable,Void> pres = new ResolvedDeferred<>(null);
        final Count res = new Count();
        Assert.assertEquals(0, res.count);
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        pres.resolve(null);
        Assert.assertEquals(1, res.count);

        final Deferred<Object,Throwable,Void> prej = new ResolvedDeferred<>(null);
        final Count rej = new Count();
        Assert.assertEquals(0, rej.count);
        prej.onComplete(rej);
        Assert.assertEquals(1, rej.count);
        prej.reject(new Throwable());
        Assert.assertEquals(1, res.count);
    }

    @Test
    public void promiseRejectTest() throws Exception {
        final Object val = new Object();
        final Deferred<Object,Throwable,Void> p = new ResolvedDeferred<>(val);
        final boolean[] called = new boolean[] { false, false };
        p.reject(new Throwable()); //Should do nothing, other implementations can throw a ResolvedException
        p.resolve(null); //Should also do nothing
        p.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                if (!p.isResolved()) {
                    Assert.fail("Expected RESOLVED found REJECTED");
                }
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                Assert.fail("Called #onReject for ResolvedDeferred");
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
            final Deferred<Object,Throwable,Void> p = new ResolvedDeferred<>(null);
            p.resolve(val);
            p.resolve(val);
        }
        {
            final Deferred<Object,Throwable,Void> p = new ResolvedDeferred<>(null);
            p.resolve(val);
            p.reject(new Throwable());
        }
    }

    @Test
    public void promiseRepeatRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final Deferred<Object,Throwable,Void> p = new ResolvedDeferred<>(null);
            p.reject(val);
            p.reject(val);
        }
        {
            final Deferred<Object,Throwable,Void> p = new ResolvedDeferred<>(null);
            p.reject(val);
            p.resolve(new Object());
        }
    }
}
