package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface OnResolve<T> {

    void resolve(final T that);
}
