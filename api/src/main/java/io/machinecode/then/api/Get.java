package io.machinecode.then.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Get<T> {

    T get() throws InterruptedException, ExecutionException;

    T get(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException;
}
