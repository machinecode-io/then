package io.machinecode.then.core.test;

import io.machinecode.then.api.Chain;
import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.OnReject;
import io.machinecode.then.api.OnResolve;
import io.machinecode.then.api.Promise;
import io.machinecode.then.core.AllChain;
import io.machinecode.then.core.ChainImpl;
import io.machinecode.then.core.PromiseImpl;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ChainTest {

    private static class Def extends ChainImpl<Chain<?>> {

        public void execute(final Chain<?> that) {
            try {
                link(that);
                resolve(that);
            } finally {
                signalAll();
            }
        }
    }

    @Test
    @Ignore
    public void basicChainTest() throws Exception {
        final Def after = new Def();

        final Def p1 = new Def();
        final Def p2 = new Def();
        final Def p3 = new Def();
        final Def p4 = new Def();

        final AllChain<Deferred<?>> all = new AllChain<Deferred<?>>(p1, p2, p3, p4);

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
                all.cancel(true);
            }
        }).start();

        latch.await();
        Thread.sleep(100);
        p1.execute(after);
        p2.execute(after);

        all.cancel(true);

        all.get(100, TimeUnit.MILLISECONDS);
    }
}
