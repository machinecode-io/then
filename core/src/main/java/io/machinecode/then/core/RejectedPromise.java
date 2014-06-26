package io.machinecode.then.core;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class RejectedPromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    public RejectedPromise(final F failure) {
        reject(failure);
    }
}
