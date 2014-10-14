package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;
import io.machinecode.then.api.Promise;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A promise that will be resolved when any of the promised passes to it are resolved. If none of them are resolved
 * this promise will be rejected with {@code null}.</p>
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class AnyDeferred<T,F,P> extends DeferredImpl<T,F,P> {

    private static final Logger log = Logger.getLogger(AnyDeferred.class);

    public AnyDeferred(final Collection<? extends Promise<?,?,?>> promises) {
        if (promises.isEmpty()) {
            log.tracef(Messages.get("THEN-000500.promise.none.resolved.in.any"));
            reject(null);
            return;
        }
        final AtomicInteger count = new AtomicInteger(0);
        for (final Promise<?,?,?> promise : promises) {
            promise.onComplete(new OnComplete() {
                @Override
                public void complete(final int state) {
                    final int n = count.incrementAndGet();
                    if (state == RESOLVED) {
                        resolve(null);
                    } else if (n == promises.size()) {
                        log.tracef(Messages.get("THEN-000500.promise.none.resolved.in.any"));
                        reject(null);
                    }
                }
            });
        }
    }

    public AnyDeferred(final Promise<?,?,?>... promises) {
        if (promises.length == 0) {
            log.tracef(Messages.get("THEN-000500.promise.none.resolved.in.any"));
            reject(null);
            return;
        }
        final AtomicInteger count = new AtomicInteger(0);
        for (final Promise<?,?,?> promise : promises) {
            promise.onComplete(new OnComplete() {
                @Override
                public void complete(final int state) {
                    final int n = count.incrementAndGet();
                    if (state == RESOLVED) {
                        resolve(null);
                    } else if (n == promises.length) {
                        log.tracef(Messages.get("THEN-000500.promise.none.resolved.in.any"));
                        reject(null);
                    }
                }
            });
        }
    }
}
