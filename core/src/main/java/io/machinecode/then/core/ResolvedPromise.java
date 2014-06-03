package io.machinecode.then.core;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ResolvedPromise<T> extends PromiseImpl<T> {

    public ResolvedPromise(final T value) {
        resolve(value);
    }
}
