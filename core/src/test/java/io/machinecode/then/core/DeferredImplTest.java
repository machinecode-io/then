package io.machinecode.then.core;

import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnProgress;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class DeferredImplTest extends UnitTest {

    @Test
    public void promiseCompleteTest() throws Exception {
        {
            final DeferredImpl<Object,Throwable,Void> d = new DeferredImpl<>();
            final Count<?,?,?> c = new Count<>();
            d.onComplete(c);
            assertEquals(0, c.count);
            assertFalse(d.isDone());

            d.resolve(null);
            assertEquals(1, c.count);

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isResolved());
            assertTrue(d.isDone());
        }
        {
            final DeferredImpl<Object,Throwable,Void> d = new DeferredImpl<>();
            final Count<?,?,?> c = new Count<>();
            d.onComplete(c);
            assertEquals(0, c.count);
            assertFalse(d.isDone());

            d.reject(new Throwable());
            assertEquals(1, c.count);

            d.reject(null);
            assertEquals(1, c.count);
            d.resolve(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isRejected());
            assertTrue(d.isDone());
        }
        {
            final DeferredImpl<Object,Throwable,Void> d = new DeferredImpl<>();
            final Count<?,?,?> c = new Count<>();
            d.onComplete(c);
            assertEquals(0, c.count);
            assertFalse(d.isDone());

            d.cancel(true);
            assertEquals(1, c.count);

            d.reject(null);
            assertEquals(1, c.count);
            d.resolve(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isCancelled());
            assertTrue(d.isDone());
        }
    }

    @Test
    public void resolveListenersCalledTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void> d = new DeferredImpl<>();
        final Count<Object,Throwable,Void> c = new Count<>();
        assertEquals(0, c.count);
        assertFalse(d.isResolved());
        assertFalse(d.isDone());

        d.resolve(null);

        assertEquals(0, c.count);
        assertTrue(d.isResolved());
        assertTrue(d.isDone());

        d.onReject(c);
        assertEquals(0, c.count);
        d.onCancel(c);
        assertEquals(0, c.count);

        d.onComplete(c);
        assertEquals(1, c.count);
        d.onComplete(c);
        assertEquals(2, c.count);

        d.onResolve(c);
        assertEquals(3, c.count);
        d.onResolve(c);
        assertEquals(4, c.count);

        d.onGet(c);

        d.get();
        assertEquals(5, c.count);

        d.get(10, TimeUnit.MILLISECONDS);
        assertEquals(6, c.count);

        d.onProgress(c);
        assertEquals(6, c.count);

        d.progress(null);
        assertEquals(7, c.count);
        d.progress(null);
        assertEquals(8, c.count);

        d.onProgress(c);
        assertEquals(8, c.count);
    }

    @Test
    public void rejectListenersCalledTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void> d = new DeferredImpl<>();
        final Count<Object,Throwable,Void> c = new Count<>();
        assertEquals(0, c.count);
        assertFalse(d.isRejected());
        assertFalse(d.isDone());

        d.reject(null);

        assertEquals(0, c.count);
        assertTrue(d.isRejected());
        assertTrue(d.isDone());

        d.onResolve(c);
        assertEquals(0, c.count);
        d.onCancel(c);
        assertEquals(0, c.count);

        d.onComplete(c);
        assertEquals(1, c.count);
        d.onComplete(c);
        assertEquals(2, c.count);

        d.onReject(c);
        assertEquals(3, c.count);
        d.onReject(c);
        assertEquals(4, c.count);

        d.onGet(c);

        try {
            d.get();
            fail();
        } catch (final Exception e) {}
        assertEquals(5, c.count);

        try {
            d.get(10, TimeUnit.MILLISECONDS);
            fail();
        } catch (final Exception e) {}
        assertEquals(6, c.count);

        d.onProgress(c);
        assertEquals(6, c.count);

        d.progress(null);
        assertEquals(7, c.count);
        d.progress(null);
        assertEquals(8, c.count);

        d.onProgress(c);
        assertEquals(8, c.count);
    }

    @Test
    public void cancelListenersCalledTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void> d = new DeferredImpl<>();
        final Count<Object,Throwable,Void> c = new Count<>();
        assertEquals(0, c.count);
        assertFalse(d.isCancelled());
        assertFalse(d.isDone());

        d.cancel(true);

        assertEquals(0, c.count);
        assertTrue(d.isCancelled());
        assertTrue(d.isDone());

        d.onResolve(c);
        assertEquals(0, c.count);
        d.onReject(c);
        assertEquals(0, c.count);

        d.onComplete(c);
        assertEquals(1, c.count);
        d.onComplete(c);
        assertEquals(2, c.count);

        d.onCancel(c);
        assertEquals(3, c.count);
        d.onCancel(c);
        assertEquals(4, c.count);

        d.onGet(c);

        try {
            d.get();
            fail();
        } catch (final Exception e) {}
        assertEquals(5, c.count);

        try {
            d.get(10, TimeUnit.MILLISECONDS);
            fail();
        } catch (final Exception e) {}
        assertEquals(6, c.count);

        d.onProgress(c);
        assertEquals(6, c.count);

        d.progress(null);
        assertEquals(7, c.count);
        d.progress(null);
        assertEquals(8, c.count);

        d.onProgress(c);
        assertEquals(8, c.count);
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
                    fail("Expected RESOLVED found REJECTED");
                }
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                called[1] = true;
                assertSame("Wrong object provided to #onResolve", val, that);
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                fail("Called #onReject on #resolve");
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean mayInterrupt) {
                fail("Called #onCancel on #resolve");
                return false;
            }
        }).resolve(val);
        assertTrue(called[0]);
        assertTrue(called[1]);
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
                    fail("Expected RESOLVED found REJECTED");
                }
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                called[1] = true;
                assertSame("Wrong exception provided to #onReject", val, fail);
            }
        }).onResolve(new OnResolve<Object>() {
            @Override
            public void resolve(final Object that) {
                fail("Called #onResolve on #reject");
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean mayInterrupt) {
                fail("Called #onCancel on #reject");
                return false;
            }
        }).reject(val);
        assertTrue(called[0]);
        assertTrue(called[1]);
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
                    fail("Expected CANCELLED found RESOLVED or REJECTED");
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
                fail("Called #onResolve on #cancel");
            }
        }).onReject(new OnReject<Throwable>() {
            @Override
            public void reject(final Throwable fail) {
                fail("Called #onReject on #cancel");
            }
        }).cancel(true);
        assertTrue(called[0]);
        assertTrue(called[1]);
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
        assertTrue(a.isResolved());
        assertFalse(b.isDone());
        assertFalse(c.isDone());
        assertFalse(ret[0]);

        b.cancel(true);
        l.await();
        assertTrue(a.isResolved());
        assertTrue(b.isCancelled());
        assertTrue(c.isCancelled());
        assertTrue(ret[0]);
    }

    @Test
    public void multipleListenersCalledResolveTest() {
        final boolean[] called = new boolean[] { false, false, false, false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onResolve(new OnResolve<Void>() {
            @Override
            public void resolve(final Void that) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onResolve(new OnResolve<Void>() {
            @Override
            public void resolve(final Void that) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onResolve(new OnResolve<Void>() {
            @Override
            public void resolve(final Void that) {
                called[2] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[3] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[4] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[5] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.resolve(null);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledRejectTest() {
        final boolean[] called = new boolean[] { false, false, false, false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onReject(new OnReject<Void>() {
            @Override
            public void reject(final Void that) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onReject(new OnReject<Void>() {
            @Override
            public void reject(final Void that) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onReject(new OnReject<Void>() {
            @Override
            public void reject(final Void that) {
                called[2] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[3] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[4] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[5] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.reject(null);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledCancelTest() {
        final boolean[] called = new boolean[] { false, false, false, false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean interrupt) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean interrupt) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onCancel(new OnCancel() {
            @Override
            public boolean cancel(final boolean interrupt) {
                called[2] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[3] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[4] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[5] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.cancel(true);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledOnlyCompleteResolveTest() {
        final boolean[] called = new boolean[] { false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[2] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.resolve(null);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledOnlyCompleteRejectTest() {
        final boolean[] called = new boolean[] { false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[2] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.reject(null);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledOnlyCompleteCancelTest() {
        final boolean[] called = new boolean[] { false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onComplete(new OnComplete() {
            @Override
            public void complete(final int state) {
                called[2] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.cancel(true);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledProgressTest() {
        final boolean[] called = new boolean[] { false, false, false };
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        d.onProgress(new OnProgress<Void>() {
            @Override
            public void progress(final Void that) {
                called[0] = true;
                throw new RuntimeException();
            }
        }).onProgress(new OnProgress<Void>() {
            @Override
            public void progress(final Void that) {
                called[1] = true;
                throw new RuntimeException();
            }
        }).onProgress(new OnProgress<Void>() {
            @Override
            public void progress(final Void that) {
                called[2] = true;
                throw new RuntimeException();
            }
        });
        try {
            d.progress(null);
            fail();
        } catch (final RuntimeException e) {
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledResolveGetTest() {
        {
            final boolean[] called = new boolean[] { false, false, false };
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            d.onGet(new ThrowingFuture<>(called, 0))
                    .onGet(new ThrowingFuture<>(called, 1))
                    .onGet(new ThrowingFuture<>(called, 2));
            d.resolve(null);
            try {
                d.get();
            } catch (final InterruptedException | ExecutionException | CancellationException e) {
                fail();
            } catch (final RuntimeException e) {
                //TODO The listeners exceptions aren't thrown at the moment, but maybe they should be
                throw new AssertionError(e);
            }
            assertArrayIs(true, called);
        }
        {
            final boolean[] called = new boolean[] { false, false, false };
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            d.onGet(new ThrowingFuture<>(called, 0))
                    .onGet(new ThrowingFuture<>(called, 1))
                    .onGet(new ThrowingFuture<>(called, 2));
            d.resolve(null);
            try {
                d.get(10, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException | ExecutionException | CancellationException | TimeoutException e) {
                fail();
            } catch (final RuntimeException e) {
                //TODO The listeners exceptions aren't thrown at the moment, but maybe they should be
                throw new AssertionError(e);
            }
            assertArrayIs(true, called);
        }
    }

    @Test
    public void multipleListenersCalledRejectGetTest() {
        {
            final boolean[] called = new boolean[] { false, false, false };
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            d.onGet(new ThrowingFuture<>(called, 0))
                    .onGet(new ThrowingFuture<>(called, 1))
                    .onGet(new ThrowingFuture<>(called, 2));
            d.reject(null);
            try {
                d.get();
                fail();
            } catch (final InterruptedException | CancellationException e) {
                fail();
            } catch (final ExecutionException e) {
                assertArrayIs(true, called);
            }
        }
        {
            final boolean[] called = new boolean[] { false, false, false };
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            d.onGet(new ThrowingFuture<>(called, 0))
                    .onGet(new ThrowingFuture<>(called, 1))
                    .onGet(new ThrowingFuture<>(called, 2));
            d.reject(null);
            try {
                d.get(10, TimeUnit.MILLISECONDS);
                fail();
            } catch (final InterruptedException | CancellationException | TimeoutException e) {
                fail();
            } catch (final ExecutionException e) {
                assertArrayIs(true, called);
            }
        }
    }

    @Test
    public void onlyValidListenersTest() {
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        try {
            d.onResolve(null);
            fail();
        } catch (final Throwable e) {
            // no op
        }
        try {
            d.onReject(null);
            fail();
        } catch (final Throwable e) {
            // no op
        }
        try {
            d.onCancel(null);
            fail();
        } catch (final Throwable e) {
            // no op
        }
        try {
            d.onComplete(null);
            fail();
        } catch (final Throwable e) {
            // no op
        }
        try {
            d.onProgress(null);
            fail();
        } catch (final Throwable e) {
            // no op
        }
        try {
            d.onGet(null);
            fail();
        } catch (final Throwable e) {
            // no op
        }
    }

    @Test
    public void fakePromiseTest() {
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        // This is a bit of a cop out. The promise should really be tested
        // but seeing as we have already tested the Deferred and we know it
        // is the same object, we know it will work
        assertSame(d, d.promise());
    }

    @Test(expected = TimeoutException.class)
    public void timeoutTest() throws TimeoutException {
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        try {
            d.get(10, TimeUnit.MILLISECONDS);
            fail();
        } catch (final InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void interruptTest() {
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        Thread.currentThread().interrupt();
        try {
            d.get();
            fail();
        } catch (final ExecutionException e) {
            fail();
        } catch (final InterruptedException e) {
            //
        }
        Thread.currentThread().interrupt();
        try {
            d.get(10, TimeUnit.MILLISECONDS);
            fail();
        } catch (final TimeoutException | ExecutionException e) {
            fail();
        } catch (final InterruptedException e) {
            //
        }
    }

    @Test
    public void awaitTest() {
        final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                d.resolve(null);
            }
        }).start();
        try {
            try {
                d.get(10, TimeUnit.MILLISECONDS);
            } catch (final TimeoutException e) {
                latch.countDown();
            }
            d.get();
        } catch (final ExecutionException | InterruptedException e) {
            fail();
        }
    }

    @Test
    public void toStringTest() {
        {
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            final String p = d.toString();
            assertNotNull(p);
            assertTrue(p.contains("PENDING"));

            d.resolve(null);

            final String r = d.toString();
            assertNotNull(r);
            assertTrue(r.contains("RESOLVED"));
        }
        {
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            d.reject(null);
            final String r = d.toString();
            assertNotNull(r);
            assertTrue(r.contains("REJECTED"));
        }
        {
            final DeferredImpl<Void,Void,Void> d = new DeferredImpl<>();
            d.cancel(true);
            final String r = d.toString();
            assertNotNull(r);
            assertTrue(r.contains("CANCELLED"));
        }
    }
}
