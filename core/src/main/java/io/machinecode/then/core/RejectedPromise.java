package io.machinecode.then.core;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class RejectedPromise<T> extends PromiseImpl<T> {

    public RejectedPromise(final Throwable failure) {
        reject(failure);
    }
}
