package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface OnReject<F extends Throwable> {

    void reject(final F fail);
}
