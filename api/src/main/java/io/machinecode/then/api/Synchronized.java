package io.machinecode.then.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Synchronized {

    void await(final Sync sync) throws InterruptedException, ExecutionException;

    void await(final long timeout, final TimeUnit unit, final Sync sync) throws InterruptedException, ExecutionException, TimeoutException;

    void signal();
}
