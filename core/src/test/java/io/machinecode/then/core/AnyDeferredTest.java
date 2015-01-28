package io.machinecode.then.core;

import io.machinecode.then.api.Promise;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class AnyDeferredTest extends Assert {

    @Test
    public void arrayResolveAllTest() throws Exception {
        for (int i = 0; i < 3; ++i) {
            final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pres = new AnyDeferred<>(ares);
            final Count<?,?,?> res = new Count<>();
            pres.onComplete(res);
            assertEquals(0, res.count);
            ares[i].resolve(null);
            assertEquals(1, res.count);
            assertTrue(pres.isResolved());
        }
    }

    @Test
    public void arrayRejectAllTest() throws Exception {
        {
            final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> prej = new AnyDeferred<>(arej);
            final Count<?,?,?> rej = new Count<>();
            prej.onComplete(rej);
            assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[1].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[2].reject(new Throwable());
            assertEquals(1, rej.count);
            assertTrue(prej.isRejected());
        }
    }

    @Test
    public void arrayResolveThirdRejectOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> prej = new AnyDeferred<>(arej);
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].resolve(null);
        assertEquals(1, rej.count);
        assertTrue(prej.isResolved());
    }

    @Test
    public void arrayResolveSecondRejectOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> prej = new AnyDeferred<>(arej);
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].resolve(null);
        assertEquals(1, rej.count);
        assertTrue(prej.isResolved());
    }

    @Test
    public void arrayResolveFirstRejectOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> prej = new AnyDeferred<>(arej);
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[0].resolve(null);
        assertEquals(1, rej.count);
        assertTrue(prej.isResolved());
    }

    @Test
    public void arrayRejectFirstResolveSecondTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> prej = new AnyDeferred<>(arej);
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].resolve(null);
        assertEquals(1, rej.count);
        assertTrue(prej.isResolved());
    }

    @Test
    public void arrayCancelAllTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> pcan = new AnyDeferred<>(acan);
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[0].cancel(true);
        assertEquals(0, can.count);
        acan[1].cancel(true);
        assertEquals(0, can.count);
        acan[2].cancel(true);
        assertEquals(1, can.count);
        assertTrue(pcan.isRejected());
    }

    @Test
    public void arrayResolveThirdCancelOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> pcan = new AnyDeferred<>(acan);
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[0].cancel(true);
        assertEquals(0, can.count);
        acan[1].cancel(true);
        assertEquals(0, can.count);
        acan[2].resolve(null);
        assertEquals(1, can.count);
        assertTrue(pcan.isResolved());
    }

    @Test
    public void arrayResolveSecondCancelOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> pcan = new AnyDeferred<>(acan);
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[0].cancel(true);
        assertEquals(0, can.count);
        acan[2].cancel(true);
        assertEquals(0, can.count);
        acan[1].resolve(null);
        assertEquals(1, can.count);
        assertTrue(pcan.isResolved());
    }

    @Test
    public void arrayResolveFirstCancelOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> pcan = new AnyDeferred<>(acan);
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[1].cancel(true);
        assertEquals(0, can.count);
        acan[2].cancel(true);
        assertEquals(0, can.count);
        acan[0].resolve(null);
        assertEquals(1, can.count);
        assertTrue(pcan.isResolved());
    }

    @Test
    public void arrayResolveSecondCancelFirstTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> pcan = new AnyDeferred<>(acan);
        final Count<?,?,?> can = new Count<>();
        pcan.onComplete(can);
        assertEquals(0, can.count);
        acan[0].cancel(true);
        assertEquals(0, can.count);
        acan[1].resolve(null);
        assertEquals(1, can.count);
        assertTrue(pcan.isResolved());
    }

    @Test
    public void arrayEmptyArgsTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {};
        final Promise<Object,Void,Void> pres = new AnyDeferred<>(ares);
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(1, res.count);
        assertTrue(pres.isRejected());
    }

    @Test
    public void collectionResolveAllTest() throws Exception {
        for (int i = 0; i < 3; ++i) {
            final DeferredImpl<Object,Throwable,Void>[] ares = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pres = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(ares));
            final Count<?,?,?> res = new Count<>();
            pres.onComplete(res);
            assertEquals(0, res.count);
            ares[i].resolve(null);
            assertEquals(1, res.count);
            assertTrue(pres.isResolved());
        }
    }

    @Test
    public void collectionRejectAllTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> prej = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(arej));
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].reject(new Throwable());
        assertEquals(1, rej.count);
        assertTrue(prej.isRejected());
    }

    @Test
    public void collectionResolveThirdRejectOthersTest() throws Exception {
        final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>(),
                new DeferredImpl<Object,Throwable,Void>()
        };
        final Promise<Object,Void,Void> prej = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(arej));
        final Count<?,?,?> rej = new Count<>();
        prej.onComplete(rej);
        assertEquals(0, rej.count);
        arej[0].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[1].reject(new Throwable());
        assertEquals(0, rej.count);
        arej[2].resolve(null);
        assertEquals(1, rej.count);
        assertTrue(prej.isResolved());
    }

    @Test
    public void collectionResolveSecondRejectOthersTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> prej = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(arej));
            final Count<?,?,?> rej = new Count<>();
            prej.onComplete(rej);
            assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[2].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[1].resolve(null);
            assertEquals(1, rej.count);
            assertTrue(prej.isResolved());
    }

    @Test
    public void collectionResolveFirstRejectOthersTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> prej = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(arej));
            final Count<?,?,?> rej = new Count<>();
            prej.onComplete(rej);
            assertEquals(0, rej.count);
            arej[1].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[2].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[0].resolve(null);
            assertEquals(1, rej.count);
            assertTrue(prej.isResolved());
    }

    @Test
    public void collectionResolveSecondRejectFirstTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] arej = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> prej = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(arej));
            final Count<?,?,?> rej = new Count<>();
            prej.onComplete(rej);
            assertEquals(0, rej.count);
            arej[0].reject(new Throwable());
            assertEquals(0, rej.count);
            arej[1].resolve(null);
            assertEquals(1, rej.count);
            assertTrue(prej.isResolved());
    }

    @Test
    public void collectionCancelAllTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pcan = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(acan));
            final Count<?,?,?> can = new Count<>();
            pcan.onComplete(can);
            assertEquals(0, can.count);
            acan[0].cancel(true);
            assertEquals(0, can.count);
            acan[1].cancel(true);
            assertEquals(0, can.count);
            acan[2].cancel(true);
            assertEquals(1, can.count);
            assertTrue(pcan.isRejected());
        }

        @Test
        public void collectionResolveThirdCancelOthersTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pcan = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(acan));
            final Count<?,?,?> can = new Count<>();
            pcan.onComplete(can);
            assertEquals(0, can.count);
            acan[0].cancel(true);
            assertEquals(0, can.count);
            acan[1].cancel(true);
            assertEquals(0, can.count);
            acan[2].resolve(null);
            assertEquals(1, can.count);
            assertTrue(pcan.isResolved());
        }

    @Test
    public void collectionResolveSecondCancelOthersTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pcan = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(acan));
            final Count<?,?,?> can = new Count<>();
            pcan.onComplete(can);
            assertEquals(0, can.count);
            acan[0].cancel(true);
            assertEquals(0, can.count);
            acan[2].cancel(true);
            assertEquals(0, can.count);
            acan[1].resolve(null);
            assertEquals(1, can.count);
            assertTrue(pcan.isResolved());
    }

    @Test
    public void collectionResolveFirstCancelOthersTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pcan = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(acan));
            final Count<?,?,?> can = new Count<>();
            pcan.onComplete(can);
            assertEquals(0, can.count);
            acan[1].cancel(true);
            assertEquals(0, can.count);
            acan[2].cancel(true);
            assertEquals(0, can.count);
            acan[0].resolve(null);
            assertEquals(1, can.count);
            assertTrue(pcan.isResolved());
    }

    @Test
    public void collectionResolveSecondCancelFirstTest() throws Exception {
            final DeferredImpl<Object,Throwable,Void>[] acan = new DeferredImpl[] {
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>(),
                    new DeferredImpl<Object,Throwable,Void>()
            };
            final Promise<Object,Void,Void> pcan = new AnyDeferred<>(Arrays.<Promise<Object,?,?>>asList(acan));
            final Count<?,?,?> can = new Count<>();
            pcan.onComplete(can);
            assertEquals(0, can.count);
            acan[0].cancel(true);
            assertEquals(0, can.count);
            acan[1].resolve(null);
            assertEquals(1, can.count);
            assertTrue(pcan.isResolved());
    }

    @Test
    public void collectionEmptyArgsTest() throws Exception {
        final Promise<Object,Void,Void> pres = new AnyDeferred<>(Collections.<Promise<Object,?,?>>emptyList());
        final Count<?,?,?> res = new Count<>();
        pres.onComplete(res);
        assertEquals(1, res.count);
        assertTrue(pres.isRejected());
    }
}
