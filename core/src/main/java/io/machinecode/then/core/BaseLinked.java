package io.machinecode.then.core;

import gnu.trove.set.hash.THashSet;
import io.machinecode.then.api.Deferred;
import io.machinecode.then.api.Linked;
import io.machinecode.then.api.On;
import io.machinecode.then.api.Sync;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public abstract class BaseLinked<T> extends DeferredImpl<T> implements Linked<T> {

    private static final Logger log = Logger.getLogger(BaseLinked.class);

    protected final Queue<Wait> waiting = new LinkedList<Wait>();
    protected final Set<On<Deferred<?>>> linkListeners = new THashSet<On<Deferred<?>>>(0);

    protected void cancelling(final On<Deferred<?>> on) {
        onLink(on);
        //try { //TODO This needs to wait on the entire chain to be cancelled
        //    await(lock, condition);
        //} catch (final Exception e) {
        //    exception = new RuntimeException(Messages.format("THEN-000006.deferred.cancel.exception"), e);
        //}
    }

    @Override
    public void signal() {
        lock.lock();
        try {
            lock.signalAll();
            Wait that;
            while ((that = waiting.poll()) != null) {
                that.getLock().lock();
                try {
                    that.getCondition().signalAll();
                } finally {
                    that.getLock().unlock();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public abstract void await(final Sync sync) throws InterruptedException;

    @Override
    public abstract void await(final long timeout, final TimeUnit unit, final Sync sync) throws InterruptedException, TimeoutException;

    @Override
    protected Logger log() {
        return log;
    }

    protected class Wait {
        final Lock lock;
        final Condition condition;

        private Wait(final Lock lock, final Condition condition) {
            this.lock = lock;
            this.condition = condition;
        }

        public Lock getLock() {
            return lock;
        }

        public Condition getCondition() {
            return condition;
        }
    }
}
