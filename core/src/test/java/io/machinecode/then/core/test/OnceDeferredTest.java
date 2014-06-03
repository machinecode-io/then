package io.machinecode.then.core.test;

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;
import io.machinecode.then.core.OnceDeferred;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class OnceDeferredTest {

    //Test OnceDeferred which should throw various CompletedException if completion is attempted twice

    @Test
    public void deferredAlreadyResolvedTest() throws Exception {
        final Object val = new Object();
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.resolve(val);
            p.resolve(val);
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }

        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.resolve(val);
            p.reject(new Throwable());
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.resolve(val);
            p.cancel();
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
        final OnceDeferred<Object> p = new OnceDeferred<Object>();
        p.resolve(val);
        p.cancel(true);
        // Cancel should be allowed to work with Future
    }

    @Test
    public void deferredAlreadyRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.reject(val);
            p.reject(val);
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.reject(val);
            p.resolve(new Object());
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.reject(val);
            p.cancel();
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        final OnceDeferred<Object> p = new OnceDeferred<Object>();
        p.reject(val);
        p.cancel(true);
        // Cancel should be allowed to work with Future
    }

    @Test
    public void deferredAlreadyCancelledTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.cancel();
            p.reject(val);
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.cancel();
            p.resolve(new Object());
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object> p = new OnceDeferred<Object>();
            p.cancel();
            p.cancel();
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        final OnceDeferred<Object> p = new OnceDeferred<Object>();
        p.cancel();
        p.cancel(true);
        // Cancel should be allowed to work with Future
    }
}
