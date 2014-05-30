package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface WhenResolved<T> {

    void resolve(final T that);
}
