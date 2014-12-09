package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.Promise;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A promise that will be resolved when all the promised passes to it are resolved.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class AllDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    final AtomicInteger count = new AtomicInteger(0);

    public AllDeferred(final Collection<? extends Promise<?,?,?>> promises) {
        if (promises.isEmpty()) {
            resolve(null);
            return;
        }
        for (final Promise<?,?,?> promise : promises) {
            promise.onComplete(new OnComplete() {
                @Override
                public void complete(final int state) {
                    if (count.incrementAndGet() == promises.size()) {
                        resolve(null);
                    }
                }
            });
        }
    }

    public AllDeferred(final Promise<?,?,?>... promises) {
        if (promises.length == 0) {
            resolve(null);
            return;
        }
        for (final Promise<?,?,?> promise : promises) {
            promise.onComplete(new OnComplete() {
                @Override
                public void complete(final int state) {
                    if (count.incrementAndGet() == promises.length) {
                        resolve(null);
                    }
                }
            });
        }
    }
}
