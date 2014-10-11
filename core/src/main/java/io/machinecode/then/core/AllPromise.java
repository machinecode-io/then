package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.Promise;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A promise that will be resolved when all the promised passes to it are resolved.
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class AllPromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    final AtomicInteger count = new AtomicInteger(0);

    public AllPromise(final Collection<Promise<?, ?>> promises) {
        if (promises.isEmpty()) {
            resolve(null);
            return;
        }
        for (final Promise<?,?> promise : promises) {
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

    public AllPromise(final Promise<?, ?>... promises) {
        if (promises.length == 0) {
            resolve(null);
            return;
        }
        for (final Promise<?,?> promise : promises) {
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
