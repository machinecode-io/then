package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.Promise;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A promise that will be resolved when all the promised passed to it are completed.
 * The outcome of them is ignored.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class WhenDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    final AtomicInteger count = new AtomicInteger(0);

    public WhenDeferred(final Collection<? extends Promise<?,?,?>> promises) {
        if (promises.isEmpty()) {
            resolve(null);
            return;
        }
        final OnComplete complete = new OnComplete() {
            @Override
            public void complete(final int state) {
                if (count.incrementAndGet() == promises.size()) {
                    resolve(null);
                }
            }
        };
        for (final Promise<?,?,?> promise : promises) {
            promise.onComplete(complete);
        }
    }

    public WhenDeferred(final Promise<?,?,?>... promises) {
        this(Arrays.asList(promises));
    }
}
