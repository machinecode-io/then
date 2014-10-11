package io.machinecode.then.core;

import io.machinecode.then.api.CompletionException;
import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.Promise;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A promise that will be resolved when any of the promised passes to it are resolved.
 * If none of them are resolved this promise will be rejected with a {@link CompletionException}.
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class AnyPromise<T> extends PromiseImpl<T,Throwable> {

    public AnyPromise(final Collection<Promise<?, ?>> promises) {
        if (promises.isEmpty()) {
            reject(new CompletionException(Messages.get("THEN-000019.promise.none.resolved.in.any")));
            return;
        }
        final AtomicInteger count = new AtomicInteger(0);
        for (final Promise<?,?> promise : promises) {
            promise.onComplete(new OnComplete() {
                @Override
                public void complete(final int state) {
                    final int n = count.incrementAndGet();
                    if (state == RESOLVED) {
                        resolve(null);
                    } else if (n == promises.size()) {
                        reject(new CompletionException(Messages.get("THEN-000019.promise.none.resolved.in.any")));
                    }
                }
            });
        }
    }

    public AnyPromise(final Promise<?, ?>... promises) {
        if (promises.length == 0) {
            reject(new CompletionException(Messages.get("THEN-000019.promise.none.resolved.in.any")));
            return;
        }
        final AtomicInteger count = new AtomicInteger(0);
        for (final Promise<?,?> promise : promises) {
            promise.onComplete(new OnComplete() {
                @Override
                public void complete(final int state) {
                    final int n = count.incrementAndGet();
                    if (state == RESOLVED) {
                        resolve(null);
                    } else if (n == promises.length) {
                        reject(new CompletionException(Messages.get("THEN-000019.promise.none.resolved.in.any")));
                    }
                }
            });
        }
    }
}
