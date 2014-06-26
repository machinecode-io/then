package io.machinecode.then.core;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ResolvedPromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    public ResolvedPromise(final T value) {
        resolve(value);
    }
}
