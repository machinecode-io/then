package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Then<T, F extends Throwable> {

    void then(final T that);

    void fail(final F failure);
}
