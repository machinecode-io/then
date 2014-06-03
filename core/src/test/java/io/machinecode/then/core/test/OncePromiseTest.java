package io.machinecode.then.core.test;

import io.machinecode.then.api.RejectedException;
import io.machinecode.then.api.ResolvedException;
import io.machinecode.then.core.OncePromise;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class OncePromiseTest {
    //Test OncePromise which should throw various CompletedException if completion is attempted twice

    @Test
    public void promiseAlreadyResolvedTest() throws Exception {
        final Object val = new Object();
        try {
            final OncePromise<Object> p = new OncePromise<Object>();
            p.resolve(val);
            p.resolve(val);
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
        try {
            final OncePromise<Object> p = new OncePromise<Object>();
            p.resolve(val);
            p.reject(new Throwable());
            Assert.fail();
        } catch (final ResolvedException e) {
            //Expected
        }
    }

    @Test
    public void promiseAlreadyRejectedTest() throws Exception {
        final Throwable val = new Throwable();
        try {
            final OncePromise<Object> p = new OncePromise<Object>();
            p.reject(val);
            p.reject(val);
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
        try {
            final OncePromise<Object> p = new OncePromise<Object>();
            p.reject(val);
            p.resolve(new Object());
            Assert.fail();
        } catch (final RejectedException e) {
            //Expected
        }
    }
}
