package io.machinecode.then.core.test;

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;
import io.machinecode.then.core.OncePromise;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class OncePromiseTest {

    //Test OncePromise which should throw various CompletionException if completion is attempted twice

    @Test
    public void deferredAlreadyResolvedTest() throws Exception {
        final Object val = new Object();
        try {
            final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
            p.resolve(val);
            p.resolve(val);
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }

        try {
            final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
            p.resolve(val);
            p.reject(new Throwable());
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
        final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
        p.resolve(val);
        p.cancel(true);
    }

    @Test
    public void deferredAlreadyRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
            p.reject(val);
            p.reject(val);
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        try {
            final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
            p.reject(val);
            p.resolve(new Object());
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
        p.reject(val);
        p.cancel(true);
    }

    @Test
    public void deferredAlreadyCancelledTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
            p.cancel(true);
            p.reject(val);
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        try {
            final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
            p.cancel(true);
            p.resolve(new Object());
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        final OncePromise<Object,Throwable> p = new OncePromise<Object,Throwable>();
        p.cancel(true);
        p.cancel(true);
        // Cancel should be allowed to work with Future
    }
}
