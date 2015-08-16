/*
 * Copyright 2015 Brent Douglas and other contributors
 * as indicated by the @authors tag. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.machinecode.then.core;

import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnCancel;
import io.machinecode.then.api.OnProgress;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Progress;
import io.machinecode.then.api.Promise;
import io.machinecode.then.api.Reject;
import io.machinecode.then.api.Resolve;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class ThenTest extends UnitTest {

    @Test
    public void thenResolveResolveTest() throws Exception {
        final DeferredImpl<String,Throwable,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.resolve(that.substring(2));
                }
            }).onComplete(c).onResolve(new OnResolve<String>() {
                @Override
                public void resolve(final String that) {
                    ref.set(that);
                }
            });

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.resolve("asdf");
            assertEquals(1, c.count);
            assertEquals("df", ref.get());

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isResolved());
            assertTrue(d.isDone());
            assertTrue(p.isResolved());
            assertTrue(p.isDone());
        }
        {
            final AtomicReference<Throwable> ref = new AtomicReference<>();
            final Throwable failure = new Throwable();
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.reject(failure);
                }
            }).onComplete(c).onReject(new OnReject<Throwable>() {
                @Override
                public void reject(final Throwable fail) {
                    ref.set(fail);
                }
            });

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());

            assertSame(failure, ref.get());
        }
        {
            final AtomicBoolean ref = new AtomicBoolean(false);
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.cancel(true);
                }
            }).onComplete(c).onCancel(new OnCancel() {
                @Override
                public boolean cancel(final boolean mayInterrupt) {
                    ref.set(true);
                    return true;
                }
            });

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());

            assertTrue(ref.get());
        }
    }

    @Test
    public void thenResolveRejectTest() throws Exception {
        final DeferredImpl<String,Throwable,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.resolve(that);
                }
            }).onComplete(c);

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.reject(null);
            assertEquals(1, c.count);

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isRejected());
            assertTrue(d.isDone());
            assertTrue(p.isRejected());
            assertTrue(p.isDone());
        }
        {
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.reject(null);
                }
            }).onComplete(c);

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());
        }
        {
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.cancel(true);
                }
            }).onComplete(c);

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());
        }
    }

    @Test
    public void thenResolveCancelTest() throws Exception {
        final DeferredImpl<String,Throwable,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.resolve(that);
                }
            }).onComplete(c);

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.cancel(true);
            assertEquals(1, c.count);

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isCancelled());
            assertTrue(d.isDone());
            assertTrue(p.isCancelled());
            assertTrue(p.isDone());
        }
        {
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.reject(null);
                }
            }).onComplete(c);

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());
        }
        {
            final Promise<String, Throwable, Void> p = d.then(new Resolve<String, String, Throwable, Void>() {
                @Override
                public void resolve(final String that, final Deferred<String, Throwable, Void> next) {
                    next.cancel(true);
                }
            }).onComplete(c);

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());
        }
    }

    @Test
    public void thenRejectResolveTest() throws Exception {
        final DeferredImpl<String,String,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final AtomicReference<Object> ref = new AtomicReference<>();
            final Promise<Object, String, Void> p = d.then(new Reject<String, String, Object, String, Void>() {
                @Override
                public void resolve(final String that, final Deferred<Object, String, Void> next) {
                    next.resolve(that.substring(2));
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onResolve(new OnResolve<Object>() {
                @Override
                public void resolve(final Object that) {
                    ref.set(that);
                }
            });

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.resolve("asdf");
            assertEquals(1, c.count);
            assertEquals("df", ref.get());

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isResolved());
            assertTrue(d.isDone());
            assertTrue(p.isResolved());
            assertTrue(p.isDone());
        }
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    next.reject((String)that);
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onReject(new OnReject<String>() {
                @Override
                public void reject(final String fail) {
                    ref.set(fail);
                }
            });

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());

            assertEquals("asdf", ref.get());
        }
        {
            final AtomicBoolean ref = new AtomicBoolean(false);
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    next.cancel(true);
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onCancel(new OnCancel() {
                @Override
                public boolean cancel(final boolean mayInterrupt) {
                    ref.set(true);
                    return false;
                }
            });

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());

            assertTrue(ref.get());
        }
    }

    @Test
    public void thenRejectRejectTest() throws Exception {
        final DeferredImpl<Object,String,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<String, String, Void> p = d.then(new Reject<Object, String, String, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<String, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final String that, final Deferred<String, String, Void> next) {
                    next.resolve(that.substring(2));
                }
            }).onComplete(c).onResolve(new OnResolve<String>() {
                @Override
                public void resolve(final String that) {
                    ref.set(that);
                }
            });

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.reject("asdf");
            assertEquals(1, c.count);
            assertEquals("df", ref.get());

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isRejected());
            assertTrue(d.isDone());
            assertTrue(p.isResolved());
            assertTrue(p.isDone());
        }
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    next.reject(that);
                }
            }).onComplete(c).onReject(new OnReject<String>() {
                @Override
                public void reject(final String fail) {
                    ref.set(fail);
                }
            });

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());

            assertEquals("asdf", ref.get());
        }
        {
            final AtomicBoolean ref = new AtomicBoolean(false);
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    next.cancel(true);
                }
            }).onComplete(c).onCancel(new OnCancel() {
                @Override
                public boolean cancel(final boolean mayInterrupt) {
                    ref.set(true);
                    return false;
                }
            });

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());

            assertTrue(ref.get());
        }
    }

    @Test
    public void thenProgressResolveTest() throws Exception {
        final DeferredImpl<String,String,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final AtomicReference<Object> ref = new AtomicReference<>();
            final Promise<Object, String, Void> p = d.then(new Progress<String, String, Void, Object, String, Void>() {
                @Override
                public void resolve(final String that, final Deferred<Object, String, Void> next) {
                    next.resolve(that.substring(2));
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void progress(final Void that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onResolve(new OnResolve<Object>() {
                @Override
                public void resolve(final Object that) {
                    ref.set(that);
                }
            });

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.resolve("asdf");
            assertEquals(1, c.count);
            assertEquals("df", ref.get());

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isResolved());
            assertTrue(d.isDone());
            assertTrue(p.isResolved());
            assertTrue(p.isDone());
        }
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    next.reject((String)that);
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onReject(new OnReject<String>() {
                @Override
                public void reject(final String fail) {
                    ref.set(fail);
                }
            });

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());

            assertEquals("asdf", ref.get());
        }
        {
            final AtomicBoolean ref = new AtomicBoolean(false);
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    next.cancel(true);
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onCancel(new OnCancel() {
                @Override
                public boolean cancel(final boolean mayInterrupt) {
                    ref.set(true);
                    return false;
                }
            });

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());

            assertTrue(ref.get());
        }
    }

    @Test
    public void thenProgressRejectTest() throws Exception {
        final DeferredImpl<Object,String,Void> d = new DeferredImpl<>();
        final Count<?,?,?> c = new Count<>();
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<String, String, Void> p = d.then(new Progress<Object, String, Void, String, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<String, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final String that, final Deferred<String, String, Void> next) {
                    next.resolve(that.substring(2));
                }

                @Override
                public void progress(final Void that, final Deferred<String, String, Void> next) {
                    throw new IllegalStateException();
                }
            }).onComplete(c).onResolve(new OnResolve<String>() {
                @Override
                public void resolve(final String that) {
                    ref.set(that);
                }
            });

            assertEquals(0, c.count);
            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.reject("asdf");
            assertEquals(1, c.count);
            assertEquals("df", ref.get());

            d.resolve(null);
            assertEquals(1, c.count);
            d.reject(null);
            assertEquals(1, c.count);
            d.cancel(true);
            assertEquals(1, c.count);

            assertTrue(d.isRejected());
            assertTrue(d.isDone());
            assertTrue(p.isResolved());
            assertTrue(p.isDone());
        }
        {
            final AtomicReference<String> ref = new AtomicReference<>();
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    next.reject(that);
                }
            }).onComplete(c).onReject(new OnReject<String>() {
                @Override
                public void reject(final String fail) {
                    ref.set(fail);
                }
            });

            assertEquals(2, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isRejected());

            assertEquals("asdf", ref.get());
        }
        {
            final AtomicBoolean ref = new AtomicBoolean(false);
            final Promise<Object, String, Void> p = d.then(new Reject<Object, String, Object, String, Void>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, String, Void> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final String that, final Deferred<Object, String, Void> next) {
                    next.cancel(true);
                }
            }).onComplete(c).onCancel(new OnCancel() {
                @Override
                public boolean cancel(final boolean mayInterrupt) {
                    ref.set(true);
                    return false;
                }
            });

            assertEquals(3, c.count);
            assertTrue(p.isDone());
            assertTrue(p.isCancelled());

            assertTrue(ref.get());
        }
    }

    @Test
    public void thenProgressProgressTest() throws Exception {
        final DeferredImpl<Object,Throwable,String> d = new DeferredImpl<>();
        {
            final AtomicReference<byte[]> ref = new AtomicReference<>();
            final Promise<Object, Throwable, byte[]> p = d.then(new Progress<Object, Throwable, String, Object, Throwable, byte[]>() {
                @Override
                public void resolve(final Object that, final Deferred<Object, Throwable, byte[]> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void reject(final Throwable that, final Deferred<Object, Throwable, byte[]> next) {
                    throw new IllegalStateException();
                }

                @Override
                public void progress(final String that, final Deferred<Object, Throwable, byte[]> next) {
                    next.progress(that.split("-")[0].getBytes());
                }
            }).onProgress(new OnProgress<byte[]>() {
                @Override
                public void progress(final byte[] that) {
                    ref.set(that);
                }
            });

            assertFalse(d.isDone());
            assertFalse(p.isDone());

            d.progress("asdf-fdaa");

            assertArrayEquals("asdf".getBytes(), ref.get());
        }
    }
}
