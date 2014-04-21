package io.machinecode.then.api;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Sync {

    void await() throws InterruptedException;

    void awaitUninterruptibly();

    long awaitNanos(final long nanosTimeout) throws InterruptedException;

    boolean await(final long time, final TimeUnit unit) throws InterruptedException;

    boolean awaitUntil(final Date deadline) throws InterruptedException;

    void signal();

    void signalAll();

    void lock();

    void lockInterruptibly() throws InterruptedException;

    boolean tryLock();

    boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException;

    void unlock();

    void enlist(final Sync sync);
}
