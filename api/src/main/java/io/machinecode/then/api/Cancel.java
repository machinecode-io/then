package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Cancel<T, F extends Throwable> extends Then<T, F> {

    void cancel();
}
