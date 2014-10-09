package io.machinecode.then.core;

/**
 * A promise that is set to the {@link #REJECTED} terminal state when constructed.
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 * @since 1.0
 */
public class RejectedPromise<T,F extends Throwable> extends PromiseImpl<T,F> {

    public RejectedPromise(final F failure) {
        reject(failure);
    }
}
