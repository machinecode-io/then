package io.machinecode.then.core;

import io.machinecode.then.api.CancelledException;
import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class OncePromiseTest {

    //Test OnceDeferred which should throw various CompletionException if completion is attempted twice

    @Test
    public void deferredAlreadyResolvedTest() throws Exception {
        final Object val = new Object();
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.resolve(val);
            p.resolve(val);
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }

        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.resolve(val);
            p.reject(new Throwable());
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
        final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
        p.resolve(val);
        p.cancel(true);
    }

    @Test
    public void deferredAlreadyRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.reject(val);
            p.reject(val);
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.reject(val);
            p.resolve(new Object());
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
        p.reject(val);
        p.cancel(true);
    }

    @Test
    public void deferredAlreadyCancelledTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.cancel(true);
            p.reject(val);
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        try {
            final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
            p.cancel(true);
            p.resolve(new Object());
            Assert.fail();
        } catch (final CancelledException e) {
            //Expected
        }
        final OnceDeferred<Object,Throwable,Void> p = new OnceDeferred<>();
        p.cancel(true);
        p.cancel(true);
        // Cancel should be allowed to work with Future
    }
}
