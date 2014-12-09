package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class RejectedPromiseTest {


    @Test
    public void promiseCompleteTest() throws Exception {
        final Deferred<Object,Throwable,Void> pres = new RejectedDeferred<>(null);
        final Count res = new Count();
        Assert.assertEquals(0, res.count);
        pres.onComplete(res);
        Assert.assertEquals(1, res.count);
        pres.resolve(null);
        Assert.assertEquals(1, res.count);

        final Deferred<Object,Throwable,Void> prej = new RejectedDeferred<>(null);
        final Count rej = new Count();
        Assert.assertEquals(0, rej.count);
        prej.onComplete(rej);
        Assert.assertEquals(1, rej.count);
        prej.reject(new Throwable());
        Assert.assertEquals(1, res.count);
    }

    @Test
    public void promiseResolveTest() throws Exception {
        final Throwable val = new Exception();
        final Deferred<Object,Throwable,Void> p = new RejectedDeferred<>(val);
        final boolean[] called = new boolean[] { false, false };
        p.reject(new Throwable()); //Should do nothing, other implementations can throw a ResolvedException
        p.resolve(new Object()); //Should also do nothing
        p.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                if (!p.isRejected()) {
                    Assert.fail("Expected REJECTED found RESOLVED");
                }
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                Assert.fail("Called #onReject on #resolve");
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                called[1] = true;
                Assert.assertSame("Wrong object provided to #onReject", val, fail);
            }
        });
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
    }

    @Test
    public void promiseRepeatResolvedTest() throws Exception {
        final Object val = new Object();
        {
            final Deferred<Object,Throwable,Void> p = new RejectedDeferred<>(null);
            p.resolve(val);
            p.resolve(val);
        }
        {
            final Deferred<Object,Throwable,Void> p = new RejectedDeferred<>(null);
            p.resolve(val);
            p.reject(new Throwable());
        }
    }

    @Test
    public void promiseRepeatRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        {
            final Deferred<Object,Throwable,Void> p = new RejectedDeferred<>(null);
            p.reject(val);
            p.reject(val);
        }
        {
            final Deferred<Object,Throwable,Void> p = new RejectedDeferred<>(null);
            p.reject(val);
            p.resolve(new Object());
        }
    }
}
