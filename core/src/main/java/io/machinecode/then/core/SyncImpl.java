package io.machinecode.then.core;

import io.machinecode.then.api.Sync;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class SyncImpl implements Sync {

    private final Lock lock;
    private final Condition condition;
    protected final Queue<Sync> waiting = new LinkedList<Sync>();

    public SyncImpl(final Lock lock) {
        this.lock = lock;
        this.condition = lock.newCondition();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
        return lock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public void await() throws InterruptedException {
        condition.await();
    }

    @Override
    public void awaitUninterruptibly() {
        condition.awaitUninterruptibly();
    }

    @Override
    public long awaitNanos(final long nanosTimeout) throws InterruptedException {
        return condition.awaitNanos(nanosTimeout);
    }

    @Override
    public boolean await(final long time, final TimeUnit unit) throws InterruptedException {
        return condition.await(time, unit);
    }

    @Override
    public boolean awaitUntil(final Date deadline) throws InterruptedException {
        return condition.awaitUntil(deadline);
    }

    @Override
    public void signal() {
        condition.signal();
        Sync that;
        while ((that = waiting.poll()) != null) {
            that.lock();
            try {
                that.signal();
            } finally {
                that.unlock();
            }
        }
    }

    @Override
    public void signalAll() {
        condition.signalAll();
        Sync that;
        while ((that = waiting.poll()) != null) {
            that.lock();
            try {
                that.signalAll();
            } finally {
                that.unlock();
            }
        }
    }

    @Override
    public void enlist(final Sync sync) {
        waiting.add(sync);
    }
}
